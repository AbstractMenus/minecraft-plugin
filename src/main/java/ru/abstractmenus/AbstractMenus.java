package ru.abstractmenus;

import com.tcoded.folialib.FoliaLib;
import lombok.Getter;
import lombok.Setter;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import ru.abstractmenus.api.*;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.api.variables.VariableManager;
import ru.abstractmenus.command.CommandManager;
import ru.abstractmenus.commands.AbstractMenuCommand;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.commands.VarCommand;
import ru.abstractmenus.commands.VarpCommand;
import ru.abstractmenus.commands.am.CommandOpen;
import ru.abstractmenus.commands.am.CommandReload;
import ru.abstractmenus.commands.am.CommandServe;
import ru.abstractmenus.commands.var.*;
import ru.abstractmenus.commands.varp.*;
import ru.abstractmenus.data.actions.MenuActions;
import ru.abstractmenus.data.activators.Activators;
import ru.abstractmenus.data.catalogs.Catalogs;
import ru.abstractmenus.data.properties.ItemProps;
import ru.abstractmenus.data.rules.MenuRules;
import ru.abstractmenus.handlers.*;
import ru.abstractmenus.handlers.placeholder.PlaceholderCustomHandler;
import ru.abstractmenus.handlers.placeholder.PlaceholderDefaultHandler;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.source.ConfigSources;
import ru.abstractmenus.listeners.ChatListener;
import ru.abstractmenus.listeners.InventoryListener;
import ru.abstractmenus.listeners.PlayerListener;
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
import java.util.Optional;

@Getter
public final class AbstractMenus extends JavaPlugin implements AbstractMenusPlugin {

    private static AbstractMenus instance;

    private CommandManager commandManager;
    private Metrics metrics;
    private FoliaLib foliaLib;

    @Getter
    @Setter
    public boolean isProxyMode;

    public Metrics getMetrics() {
        return metrics;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public Plugin getPlugin() {
        return this;
    }

    @Override
    public VariableManager getVariableManager() {
        return VariableManagerImpl.instance();
    }

    @Override
    public Optional<Menu> getOpenedMenu(Player player) {
        return Optional.ofNullable(MenuManager.instance().getOpenedMenu(player));
    }

    @Override
    public void onLoad() {
        AbstractMenusProvider.init(this);
        getServer().getServicesManager()
                .register(AbstractMenusPlugin.class, this, this, ServicePriority.Normal);
    }

    @Override
    public void onEnable() {
        try {
            instance = this;
            isProxyMode = determineProxy();
            Logger.set(getLogger());
            metrics = new Metrics(this);
            foliaLib = new FoliaLib(this);

            MainConfig config = new MainConfig();

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

            registerProviders();
            registerCommands();

            Serializers.init(this);
            ItemProps.init();
            Activators.init();
            MenuActions.init();
            MenuRules.init();
            Catalogs.init();

            loadMenus();

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
            disablePlugin();
        }
    }

    @Override
    public void loadMenus() {
        try {
            MenuManager.instance().loadMenus();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.severe("Error while loading menus: " + e.getMessage());
        }
    }

    @Override
    public void openMenu(Player player, Menu menu) {
        MenuManager.instance().openMenu(player, menu);
    }

    @Override
    public void openMenu(Activator activator, Object ctx, Player player, Menu menu) {
        MenuManager.instance().openMenu(activator, ctx, player, menu);
    }

    @Override
    public void onDisable() {
        if (MenuManager.instance() != null) {
            MenuManager.instance().unloadAll();
        }

        if (BungeeManager.instance() != null)
            BungeeManager.instance().stopOnlineTimer();

        VariableManagerImpl.instance().shutdown();
        Events.unregisterAll();

        getServer().getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
    }

    private void registerCommands() {
        Command am = new AbstractMenuCommand("am.admin")
                .addSub("reload", new CommandReload())
                .addSub("open", new CommandOpen())
                .addSub("serve", new CommandServe());

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

        getServer().getPluginCommand("am").setExecutor(am);
        getServer().getPluginCommand("var").setExecutor(var);
        getServer().getPluginCommand("varp").setExecutor(varp);
    }

    private void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
    }

    private void registerProviders() {
        if (checkDependency("Vault")) {
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);

            if (economyProvider != null) {
                Handlers.setEconomyHandler(new EconomyVaultHandler(economyProvider.getProvider()));
            } else {
                Logger.warning("Economy plugin doesn't installed");
            }
        } else {
            Logger.warning("Vault doesn't installed. Economy actions and rules won't work");
        }

        if (checkDependency("LuckPerms")) {
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

            if (provider != null) {
                Handlers.setPermissionsHandler(new LuckPermsHandler(provider.getProvider()));
                Logger.info("Using LuckPerms");
            } else {
                Logger.severe("Cannot find registered LuckPerms service");
            }
        } else {
            Handlers.setPermissionsHandler(new PermissionDefaultHandler(this));
            Logger.info("Using bundled temporary permissions manager");
            Logger.warning("LuckPerms doesn't installed. After reload all assigned permissions will be removed");
        }

        Handlers.setLevelHandler(new LevelDefaultHandler());

        if (checkDependency("PlaceholderAPI")) {
            Handlers.setPlaceholderHandler(new PlaceholderCustomHandler());
            Logger.info("Using PlaceholderAPI");
        } else {
            Handlers.setPlaceholderHandler(new PlaceholderDefaultHandler());
            Logger.info("Using bundled placeholders");
        }

        Handlers.getPlaceholderHandler().registerAll();

        if (checkDependency("SkinsRestorer")) {
            Handlers.setSkinHandler(new SkinsRestorerHandler(isProxyMode, this));
            Logger.info("Using SkinsRestorer as skins provider");
        }
    }

    public static boolean checkDependency(String plugin) {
        return Bukkit.getServer().getPluginManager().isPluginEnabled(plugin);
    }

    public static AbstractMenus instance() {
        return instance;
    }

    // from SkinRestorer
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
