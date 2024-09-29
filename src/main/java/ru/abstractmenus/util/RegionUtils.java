package ru.abstractmenus.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public final class RegionUtils {

    private RegionUtils(){}

    private static RegionManager getRegionManager(org.bukkit.World bukkitWorld){
        try {
            World world = BukkitAdapter.adapt(bukkitWorld);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            return container.get(world);
        } catch (IllegalAccessError e) {
            try {
                WorldGuardPlugin plugin = WorldGuardPlugin.inst();
                return (RegionManager) plugin.getClass().getMethod("getRegionManager", org.bukkit.World.class).invoke(plugin, bukkitWorld);
            } catch (ReflectiveOperationException e1) {
                System.out.println(e.getMessage());
            }
        }

        return null;
    }

    public static Iterable<ProtectedRegion> getRegions(org.bukkit.World bukkitWorld){
        RegionManager regionManager = getRegionManager(bukkitWorld);
        return (regionManager != null) ? regionManager.getRegions().values() : null;
    }

    public static ProtectedRegion getRegion(org.bukkit.World world, String name){
        RegionManager regionManager = getRegionManager(world);
        return (regionManager != null) ? regionManager.getRegion(name) : null;
    }
}
