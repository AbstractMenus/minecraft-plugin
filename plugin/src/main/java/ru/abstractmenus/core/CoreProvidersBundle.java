package ru.abstractmenus.core;

import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.handlers.EconomyVaultHandler;
import ru.abstractmenus.handlers.LevelDefaultHandler;
import ru.abstractmenus.handlers.LuckPermsHandler;
import ru.abstractmenus.handlers.PermissionDefaultHandler;
import ru.abstractmenus.handlers.SkinsRestorerHandler;
import ru.abstractmenus.handlers.placeholder.PlaceholderCustomHandler;
import ru.abstractmenus.handlers.placeholder.PlaceholderDefaultHandler;

/**
 * Registers built-in handler providers (economy, permissions, levels,
 * placeholders, skins). Mirrors the old {@code AbstractMenus.registerProviders}
 * behaviour &mdash; same conditions, same priorities &mdash; but through the
 * new {@link ru.abstractmenus.api.ProviderRegistry} SPI.
 *
 * <p>All core providers register at priority 50 so addons can override by
 * registering at priority &gt; 50.
 */
final class CoreProvidersBundle {

    private static final int CORE_PRIORITY = 50;

    void register(AbstractMenusApi api, MenuExtension owner) {
        AbstractMenus plugin = AbstractMenus.instance();

        // ---- Economy --------------------------------------------------------
        if (AbstractMenus.checkDependency("Vault")) {
            RegisteredServiceProvider<Economy> economyProvider =
                    Bukkit.getServicesManager().getRegistration(Economy.class);

            if (economyProvider != null) {
                api.providers().registerEconomy(
                        "vault",
                        new EconomyVaultHandler(economyProvider.getProvider()),
                        CORE_PRIORITY,
                        owner);
                Logger.info("Registered core economy provider: vault");
            } else {
                Logger.warning("Economy plugin doesn't installed");
            }
        } else {
            Logger.warning("Vault doesn't installed. Economy actions and rules won't work");
        }

        // ---- Permissions ----------------------------------------------------
        if (AbstractMenus.checkDependency("LuckPerms")) {
            RegisteredServiceProvider<LuckPerms> provider =
                    Bukkit.getServicesManager().getRegistration(LuckPerms.class);

            if (provider != null) {
                api.providers().registerPermissions(
                        "luckperms",
                        new LuckPermsHandler(provider.getProvider()),
                        CORE_PRIORITY,
                        owner);
                Logger.info("Using LuckPerms");
            } else {
                Logger.severe("Cannot find registered LuckPerms service");
            }
        } else {
            api.providers().registerPermissions(
                    "default",
                    new PermissionDefaultHandler(plugin),
                    CORE_PRIORITY,
                    owner);
            Logger.info("Using bundled temporary permissions manager");
            Logger.warning("LuckPerms doesn't installed. After reload all assigned permissions will be removed");
        }

        // ---- Levels ---------------------------------------------------------
        api.providers().registerLevels(
                "default",
                new LevelDefaultHandler(),
                CORE_PRIORITY,
                owner);

        // ---- Placeholders ---------------------------------------------------
        if (AbstractMenus.checkDependency("PlaceholderAPI")) {
            PlaceholderCustomHandler papiHandler = new PlaceholderCustomHandler();
            api.providers().registerPlaceholders(
                    "placeholderapi",
                    papiHandler,
                    CORE_PRIORITY,
                    owner);
            papiHandler.registerAll();
            Logger.info("Using PlaceholderAPI");
        } else {
            PlaceholderDefaultHandler defaultHandler = new PlaceholderDefaultHandler();
            api.providers().registerPlaceholders(
                    "default",
                    defaultHandler,
                    CORE_PRIORITY,
                    owner);
            defaultHandler.registerAll();
            Logger.info("Using bundled placeholders");
        }

        // ---- Skins ----------------------------------------------------------
        if (AbstractMenus.checkDependency("SkinsRestorer")) {
            api.providers().registerSkins(
                    "skinsrestorer",
                    new SkinsRestorerHandler(plugin.isProxyMode, plugin),
                    CORE_PRIORITY,
                    owner);
            Logger.info("Using SkinsRestorer as skins provider");
        }
    }
}
