package ru.abstractmenus.listeners.wg;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class WGHandlers {

    private WGHandlers() { }

    public static void initRegionListeners(Plugin plugin) {
        String version = WorldGuardPlugin.inst().getDescription().getVersion();
        int majorVer = Integer.parseInt(version.split("\\.")[0]);

        if (majorVer >= 7) {
            WorldGuard.getInstance().getPlatform()
                    .getSessionManager()
                    .registerHandler(new WgRegionHandler.Factory(), null);
        } else {
            // Session handlers works only on WorldGuard < 7.0 correctly
            Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), plugin);
        }
    }

}
