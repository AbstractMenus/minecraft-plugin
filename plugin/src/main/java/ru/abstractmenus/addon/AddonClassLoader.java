package ru.abstractmenus.addon;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Isolated classloader for an AM-loaded addon.
 *
 * <p><strong>Parent-first</strong> for {@code ru.abstractmenus.api.*},
 * {@code org.bukkit.*}, Paper, Adventure, and the JDK — so the addon and
 * the plugin share a single Class object for these. Otherwise
 * {@code addon instanceof MenuExtension} would fail across classloader
 * boundaries and Bukkit would refuse to accept the addon's event types.
 *
 * <p><strong>Child-first</strong> for everything else — the addon can ship
 * its own shaded copies of Gson, Guava, HTTP clients, etc. without
 * conflicting with what AbstractMenus (or other addons) shade.
 */
public final class AddonClassLoader extends URLClassLoader {

    static final String[] PARENT_FIRST_PREFIXES = {
            "ru.abstractmenus.api.",
            "org.bukkit.",
            "org.spigotmc.",
            "io.papermc.",
            "com.destroystokyo.paper.",
            "net.kyori.adventure.",
            // Paperweight-userdev exposes remapped NMS / Mojang internals to
            // the plugin classloader. Keep them parent-first so an addon
            // cannot ship its own copy of, say, net.minecraft.nbt.CompoundTag
            // and have it hide the real one for any code that crosses the
            // addon boundary.
            "net.minecraft.",
            "com.mojang.",
            // Server-bundled libraries. If an addon ships its own copy with
            // a different version or shaded-but-not-relocated, the same
            // class loaded by two classloaders is not the same class -
            // ClassCastException at the first hand-off. Force parent-first
            // so everyone uses the server's copy.
            "ca.spottedleaf.",       // Paper concurrent utilities
            "io.netty.",             // Netty (Paper bundles)
            "it.unimi.dsi.fastutil.",
            "com.google.gson.",
            "com.google.common.",    // Guava
            "java.",
            "javax.",
            "jdk.",
            "sun."
    };

    public AddonClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c != null) {
                if (resolve) resolveClass(c);
                return c;
            }

            // Parent-first packages — always delegate first.
            for (String prefix : PARENT_FIRST_PREFIXES) {
                if (name.startsWith(prefix)) {
                    try {
                        c = getParent().loadClass(name);
                        if (resolve) resolveClass(c);
                        return c;
                    } catch (ClassNotFoundException ignored) {
                        // fall through — parent didn't have it, try the jar
                    }
                }
            }

            // Child-first: try our own URLs before delegating.
            try {
                c = findClass(name);
                if (resolve) resolveClass(c);
                return c;
            } catch (ClassNotFoundException ignored) {
                // Fall back to parent.
            }

            // Final fallback — parent (may throw ClassNotFoundException).
            c = getParent().loadClass(name);
            if (resolve) resolveClass(c);
            return c;
        }
    }
}
