package ru.abstractmenus.addon;

import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loads, enables, and manages AM-loaded addons (the lightweight jars in
 * {@code plugins/AbstractMenus/addons/}).
 *
 * <p>Plugin-as-addons (Path 1) are not handled here — they boot through
 * Bukkit's own plugin lifecycle and look up the API via
 * {@link AbstractMenusApi#get()}.
 */
public final class AddonManager {

    private final AbstractMenus plugin;
    private final Path addonsDir;
    private final AbstractMenusApi api;

    /** name (lowercased) → LoadedAddon, insertion-ordered (matches enable order) */
    private final Map<String, LoadedAddon> addons = new LinkedHashMap<>();

    public AddonManager(AbstractMenus plugin, AbstractMenusApi api) {
        this.plugin = plugin;
        this.api = api;
        this.addonsDir = plugin.getDataFolder().toPath().resolve("addons");
    }

    /**
     * Discover, parse, sort, and enable every addon in the addons directory.
     * Safe to call once during plugin enable. If the directory doesn't exist,
     * creates it and returns without enabling anything.
     */
    public void loadAll() {
        // Impl — Tasks 6 + 7
        Logger.info("AddonManager.loadAll — not yet implemented");
    }

    /**
     * Disable every loaded addon (in reverse enable order) and release
     * classloader resources. Called from plugin onDisable.
     */
    public void unloadAll() {
        // Impl — Task 8
    }

    /**
     * Reload a single AM-loaded addon by name: disable → close classloader →
     * re-parse the jar → enable. Returns the new {@link LoadedAddon}, or
     * empty if no addon of that name is currently loaded.
     *
     * @param name addon name (case-insensitive)
     * @return the freshly loaded addon, or empty if not found / no jar present
     */
    public Optional<LoadedAddon> reload(String name) {
        // Impl — Task 9
        return Optional.empty();
    }

    public Collection<LoadedAddon> loaded() {
        return Collections.unmodifiableCollection(addons.values());
    }

    public Optional<LoadedAddon> get(String name) {
        return Optional.ofNullable(addons.get(name.toLowerCase()));
    }

    /**
     * Scan {@link #addonsDir} for {@code *.jar} files. For each, extract
     * {@code addon.conf}, parse it, and build a LoadedAddon (without enabling
     * — status stays PENDING).
     *
     * <p>Jars that are missing addon.conf, have malformed addon.conf, or
     * duplicate a name already seen are logged and skipped — not fatal.
     *
     * @return map of name (lowercased) → PENDING LoadedAddon, in discovery
     *         order (stable for the later topological sort)
     */
    Map<String, LoadedAddon> discover() {
        Map<String, LoadedAddon> pending = new LinkedHashMap<>();

        if (!java.nio.file.Files.isDirectory(addonsDir)) {
            try {
                java.nio.file.Files.createDirectories(addonsDir);
            } catch (java.io.IOException e) {
                Logger.warning("Could not create addons directory " + addonsDir + ": " + e.getMessage());
            }
            return pending;
        }

        try (var stream = java.nio.file.Files.newDirectoryStream(addonsDir, "*.jar")) {
            for (Path jar : stream) {
                try {
                    LoadedAddon addon = readAddonJar(jar);
                    String key = addon.conf().name().toLowerCase();
                    if (pending.containsKey(key)) {
                        Logger.warning("Duplicate addon name '" + addon.conf().name()
                                + "' — ignoring " + jar.getFileName());
                        try { addon.classLoader().close(); } catch (Exception ignored) {}
                        continue;
                    }
                    pending.put(key, addon);
                } catch (Exception e) {
                    Logger.warning("Failed to load addon " + jar.getFileName() + ": " + e.getMessage());
                }
            }
        } catch (java.io.IOException e) {
            Logger.warning("Failed to scan addons directory: " + e.getMessage());
        }

        return pending;
    }

    /**
     * Read a single addon jar: extract {@code addon.conf}, parse it, build a
     * classloader. Throws if addon.conf is missing or malformed.
     */
    private LoadedAddon readAddonJar(Path jarPath) throws java.io.IOException {
        String hocon;
        try (var jar = new java.util.jar.JarFile(jarPath.toFile())) {
            var entry = jar.getJarEntry("addon.conf");
            if (entry == null) {
                throw new java.io.IOException("no addon.conf at jar root");
            }
            try (var in = jar.getInputStream(entry)) {
                hocon = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
        }

        AddonConf conf = AddonConf.parse(hocon);
        AddonClassLoader cl = new AddonClassLoader(
                new java.net.URL[]{jarPath.toUri().toURL()},
                plugin.getClass().getClassLoader());

        return new LoadedAddon(conf, cl);
    }

    // package-private helpers filled in by subsequent tasks (instantiate,
    // rollbackRegistrations, findJarByName)
}
