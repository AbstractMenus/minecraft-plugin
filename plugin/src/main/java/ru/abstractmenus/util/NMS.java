package ru.abstractmenus.util;

import org.bukkit.Bukkit;

public final class NMS {

    private static volatile int minorVersion = -1;

    private NMS() {}

    public static String getVersion() {
        try {
            // Paper now has API to get version string in format <major>.<minor>.<patch>
            // Replace `.` to `_` for backward compatibility
            return Bukkit.getMinecraftVersion().replace('.', '_');
        } catch (Throwable t) {
            return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        }
    }

    public static int getMinorVersion() {
        if (minorVersion == -1) {
            String ver = getVersion();
            String[] arr = ver.split("_");
            minorVersion = arr.length > 1 ? Integer.parseInt(arr[1]) : -1;
        }

        return minorVersion;
    }
}
