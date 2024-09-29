package ru.abstractmenus.util.proxy;

import lombok.Data;

/**
 * From <a href="https://github.com/SkinsRestorer/SkinsRestorer/blob/dev/shared/src/main/java/net/skinsrestorer/shared/info/ClassInfo.java">SkinRestorer</a>
 */
@Data
public class ClassInfo {
    private static final ClassInfo INSTANCE = new ClassInfo();
    private final boolean craftBukkit;
    private final boolean spigot;
    private final boolean paper;
    private final boolean folia;
    private final boolean bungeecord;
    private final boolean velocity;

    private ClassInfo() {
        spigot = ReflectionUtil.classExists("org.spigotmc.SpigotConfig");
        paper = ReflectionUtil.classExists("com.destroystokyo.paper.PaperConfig", "io.papermc.paper.configuration.Configuration");
        craftBukkit = ReflectionUtil.classExists("org.bukkit.Bukkit");
        folia = ReflectionUtil.classExists("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
        bungeecord = ReflectionUtil.classExists("net.md_5.bungee.BungeeCord");
        velocity = ReflectionUtil.classExists("com.velocitypowered.proxy.Velocity");
    }

    public static ClassInfo get() {
        return INSTANCE;
    }
}