package ru.abstractmenus;

import com.tcoded.folialib.FoliaLib;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import ru.abstractmenus.api.*;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.impl.AbstractMenusApiImpl;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.api.variables.VariableManager;
import ru.abstractmenus.command.CommandManager;
import ru.abstractmenus.commands.AbstractMenuCommand;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.commands.VarCommand;
import ru.abstractmenus.commands.VarpCommand;
import ru.abstractmenus.commands.am.CommandAddons;
import ru.abstractmenus.commands.am.CommandOpen;
import ru.abstractmenus.commands.am.CommandPluginVersion;
import ru.abstractmenus.commands.am.CommandReload;
import ru.abstractmenus.commands.am.CommandServe;
import ru.abstractmenus.commands.var.*;
import ru.abstractmenus.commands.varp.*;
import ru.abstractmenus.addon.AddonManager;
import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.core.CoreExtension;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.source.ConfigSources;
import ru.abstractmenus.listeners.ChatListener;
import ru.abstractmenus.listeners.InventoryListener;
import ru.abstractmenus.listeners.PlayerListener;
import ru.abstractmenus.listeners.ServerLoadListener;
import ru.abstractmenus.listeners.wg.WGHandlers;
import ru.abstractmenus.nms.actionbar.ActionBar;
import ru.abstractmenus.nms.title.Title;
import ru.abstractmenus.serializers.Serializers;
import ru.abstractmenus.services.BungeeManager;
import ru.abstractmenus.services.HeadAnimManager;
import ru.abstractmenus.services.MenuManager;
import ru.abstractmenus.services.ProfileStorage;
import ru.abstractmenus.util.MiniMessageUtil;
import ru.abstractmenus.util.TimeUtil;
import ru.abstractmenus.util.bukkit.BukkitTasks;
import ru.abstractmenus.util.bukkit.Events;
import ru.abstractmenus.util.proxy.ClassInfo;
import ru.abstractmenus.variables.VariableManagerImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

@Getter
public final class AbstractMenus extends JavaPlugin {

    private static AbstractMenus instance;

    private CommandManager commandManager;
    private Metrics metrics;
    private FoliaLib foliaLib;
    private AbstractMenusApi api;
    private AddonManager addonManager;
    private MainConfig mainConfig;
    /**
     * The plugin's own dogfood {@link MenuExtension}. Held as a field so
     * {@code /am addons list} can render it as the {@code [built-in]} entry
     * and tell it apart from operator-installed Path 1 / Path 2 addons.
     */
    private MenuExtension core;

    @Getter
    @Setter
    public boolean isProxyMode;

    public Plugin getPlugin() {
        return this;
    }

    public AbstractMenusApi getApi() { return api; }

    public AddonManager getAddonManager() { return addonManager; }

    public VariableManager getVariableManager() {
        return VariableManagerImpl.instance();
    }

    public Optional<Menu> getOpenedMenu(Player player) {
        return Optional.ofNullable(MenuManager.instance().getOpenedMenu(player));
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        try {
            instance = this;
            isProxyMode = determineProxy();
            Logger.set(getLogger());
            metrics = new Metrics(this);
            foliaLib = new FoliaLib(this);

            mainConfig = new MainConfig();
            MainConfig config = mainConfig;

            config.load(this, ConfigurationLoader.builder()
                    .source(ConfigSources.resource("/config.conf", this)
                            .copyTo(getDataFolder().toPath()))
                    .build()
                    .load());

            Colors.init(!config.isUseMiniMessage());

            ConfigurationLoader headAnimConfLoader = ConfigurationLoader.builder()
                    .source(ConfigSources.resource("/animated_heads.conf", this)
                            .copyTo(getDataFolder().toPath()))
                    .build();

            commandManager = new CommandManager(this);

            new HeadAnimManager(headAnimConfLoader);
            Events.setPlugin(this);
            BukkitTasks.setPlugin(this);
            BukkitTasks.setFoliaLib(foliaLib);
            TimeUtil.init(config);
            MiniMessageUtil.init(config);
            new VariableManagerImpl(config);
            new BungeeManager(this, config);
            ActionBar.init();
            Title.init();

            new MenuManager(this, config);

            // Publish the new Extension API to Bukkit's ServicesManager so
            // plugin-as-addons (Path 1) and the dogfood CoreExtension (wired in
            // the next phase) can look it up via AbstractMenusApi.get().
            this.api = new AbstractMenusApiImpl(this);
            getServer().getServicesManager().register(
                    AbstractMenusApi.class, api, this, ServicePriority.Normal);

            registerCommands(config);

            Serializers.init(this);

            // Core extension — dogfood: the plugin registers its own types through
            // the same SPI external addons will use.
            core = new CoreExtension();
            core.onLoad(api);
            core.onEnable(api);

            // External addons (plugins/AbstractMenus/addons/*.jar) load here so
            // their registered types are available before menus parse.
            this.addonManager = new AddonManager(this, api);
            addonManager.loadAll();

            // Menus parse via ServerLoadEvent (after every plugin's onEnable
            // has completed) so Path 1 plugin-as-addons - which Bukkit only
            // enables AFTER us due to depend: AbstractMenus - have a chance to
            // register their providers before HOCON parse-time validation
            // runs. Path 2 addons are already loaded above, so they work
            // either way.
            getServer().getPluginManager().registerEvents(new ServerLoadListener(), this);

            getServer().getPluginManager().registerEvents(new InventoryListener(), this);
            getServer().getPluginManager().registerEvents(new ProfileStorage(), this);
            getServer().getPluginManager().registerEvents(new ChatListener(), this);
            getServer().getPluginManager().registerEvents(new PlayerListener(), this);

            if (config.isUseWorldGuard() && checkDependency("WorldGuard")) {
                WGHandlers.initRegionListeners(this);
                Logger.info("Using WorldGuard");
            }
        } catch (Exception e) {
            Logger.severe("Cannot enable plugin: " + e.getMessage());
            e.printStackTrace();
            // disablePlugin() from inside onEnable is re-entrant: Bukkit calls
            // our onDisable while we are still in this stack frame. Every
            // singleton accessed in onDisable is null-guarded so a partial
            // init does not NPE on the way out. Rethrowing instead would
            // skip our cleanup (service-manager registrations, listeners,
            // database connections) since Bukkit does not call onDisable
            // for plugins whose onEnable threw.
            disablePlugin();
        }
    }

    public void loadMenus() {
        try {
            MenuManager.instance().loadMenus();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.severe("Error while loading menus: " + e.getMessage());
        }
    }

    public void openMenu(Player player, Menu menu) {
        MenuManager.instance().openMenu(player, menu);
    }

    public void openMenu(Activator activator, Object ctx, Player player, Menu menu) {
        MenuManager.instance().openMenu(activator, ctx, player, menu);
    }

    @Override
    public void onDisable() {
        if (addonManager != null) addonManager.unloadAll();

        if (MenuManager.instance() != null) {
            MenuManager.instance().unloadAll();
        }

        if (BungeeManager.instance() != null)
            BungeeManager.instance().stopOnlineTimer();

        if (VariableManagerImpl.instance() != null) {
            VariableManagerImpl.instance().shutdown();
        }
        Events.unregisterAll();

        getServer().getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
        getServer().getServicesManager().unregisterAll(this);
    }

    private void registerCommands(MainConfig config) {
        Command am = new AbstractMenuCommand("am.admin")
                .addSub("reload", new CommandReload())
                .addSub("open", new CommandOpen(config))
                .addSub("serve", new CommandServe())
                .addSub("version", new CommandPluginVersion())
                .addSub("addons", new CommandAddons());

        Command var = new VarCommand("am.admin")
                .addSub("get", new VarGet())
                .addSub("set", new VarSet())
                .addSub("rem", new VarRem())
                .addSub("inc", new VarInc())
                .addSub("dec", new VarDec())
                .addSub("mul", new VarMul())
                .addSub("div", new VarDiv());

        Command varp = new VarpCommand("am.admin")
                .addSub("get", new VarpGet())
                .addSub("set", new VarpSet())
                .addSub("rem", new VarpRem())
                .addSub("inc", new VarpInc())
                .addSub("dec", new VarpDec())
                .addSub("mul", new VarpMul())
                .addSub("div", new VarpDiv());

        var amCmd = Objects.requireNonNull(getServer().getPluginCommand("am"));
        amCmd.setExecutor(am);
        amCmd.setTabCompleter(am);

        var varCmd = Objects.requireNonNull(getServer().getPluginCommand("var"));
        varCmd.setExecutor(var);
        varCmd.setTabCompleter(var);

        var varpCmd = Objects.requireNonNull(getServer().getPluginCommand("varp"));
        varpCmd.setExecutor(varp);
        varpCmd.setTabCompleter(varp);
    }

    private void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
    }

    public static boolean checkDependency(String plugin) {
        return Bukkit.getServer().getPluginManager().isPluginEnabled(plugin);
    }

    public static AbstractMenus instance() {
        return instance;
    }

    // from SkinRestorer.
    //
    // YamlConfiguration.loadConfiguration below reads spigot.yml / paper.yml
    // synchronously on the main thread. These files are <10 KB on a real
    // server and the call only fires once, during onEnable, before any
    // gameplay can be affected. Acceptable startup-only IO.
    public boolean determineProxy() {
        Path spigotFile = Paths.get("spigot.yml");
        Path paperFile = Paths.get("paper.yml");

        if (Optional.of(getConfig()).map(config ->
                config.getBoolean("settings.bungeecord")).orElse(false)) {
            return true;
        } else if (ClassInfo.get().isSpigot() // Only consider files if classes for that platform are present
                && Files.exists(spigotFile)
                && YamlConfiguration.loadConfiguration(spigotFile.toFile())
                .getBoolean("settings.bungeecord")) {
            return true;
        } else if (Optional.of(Bukkit.spigot().getPaperConfig()).map(config ->
                config.getBoolean("settings.velocity-support.enabled")
                        || config.getBoolean("proxies.velocity.enabled")).orElse(false)) {
            return true;
        } else return ClassInfo.get().isPaper() // Only consider files if classes for that platform are present
                && Files.exists(paperFile)
                && YamlConfiguration.loadConfiguration(paperFile.toFile())
                .getBoolean("settings.velocity-support.enabled");
    }
}
