package ru.abstractmenus.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Logger;

import java.lang.invoke.MethodHandle;

public abstract class NMS {

    private static final Class<?> packetClass;
    private static int minorVersion = -1;

    static {
        packetClass = getNMSClass("Packet");
    }

    public static Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + getVersion() + "." + name);
        } catch(Exception ex) {
            return null;
        }
    }

    public static Class<?> getCraftBukkitClass(String name, Package pkg){
        try {
            return Class.forName("org.bukkit.craftbukkit." + getVersion() + pkg.getName() + name);
        } catch(Exception ex) {
            return null;
        }
    }

    public static String getVersion() {
        try {
            // Paper now has API to get version string in format <major>.<minor>.<patch>
            // Replace `.` to `_` for backward compatibility
            return Bukkit.getMinecraftVersion().replace('.', '_');
        } catch (Throwable t) {
            return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        }
    }

    public static int getMinorVersion(){
        if (minorVersion == -1) {
            String ver = getVersion();
            String[] arr = ver.split("_");
            minorVersion = arr.length > 1 ? Integer.parseInt(arr[1]) : -1;
        }

        return minorVersion;
    }

    public static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", packetClass).invoke(playerConnection, packet);
        } catch(Throwable th) {
            Logger.warning("Could not send packet " + packet + " to player " + player.getName());
        }
    }

    protected enum Package {
        ROOT("."),
        BLOCK(".block."),
        COMMAND(".command."),
        INVENTORY(".inventory."),
        ENTITY(".entity.");

        private String name;

        Package(String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }
    }
}
