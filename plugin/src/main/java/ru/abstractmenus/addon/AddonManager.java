package ru.abstractmenus.addon;

import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
        Map<String, LoadedAddon> pending = discover();
        if (pending.isEmpty()) {
            Logger.info("No AM-loaded addons found in " + addonsDir);
            return;
        }

        // Soft-filter: drop addons whose required Bukkit plugin deps are missing.
        var pluginManager = plugin.getServer().getPluginManager();
        var byName = new LinkedHashMap<String, LoadedAddon>();
        for (LoadedAddon la : pending.values()) {
            AddonConf c = la.conf();
            boolean missing = false;
            for (String dep : c.pluginDependencies()) {
                if (pluginManager.getPlugin(dep) == null) {
                    Logger.warning("Addon " + c.name()
                            + " requires plugin '" + dep + "' which is not installed — skipping");
                    la.markFailed(new IllegalStateException("missing plugin dependency: " + dep));
                    missing = true;
                    break;
                }
            }
            if (missing) {
                addons.put(c.name().toLowerCase(), la);  // keep the failed entry visible in /am addons list
                continue;
            }
            byName.put(c.name().toLowerCase(), la);
        }

        if (byName.isEmpty()) return;

        // Sort by addon-level dependencies.
        Map<String, List<String>> depGraph = new LinkedHashMap<>();
        for (var e : byName.entrySet()) {
            List<String> deps = e.getValue().conf().addonDependencies().stream()
                    .map(String::toLowerCase).toList();
            depGraph.put(e.getKey(), deps);
        }
        List<String> order;
        try {
            order = AddonDependencyGraph.topoSort(depGraph);
        } catch (AddonDependencyException ex) {
            Logger.severe("Addon dependency graph error: " + ex.getMessage());
            for (var la : byName.values()) {
                la.markFailed(ex);
                addons.put(la.conf().name().toLowerCase(), la);
            }
            return;
        }

        // Stage 1: onLoad for all — ordering-independent setup.
        for (String k : order) {
            LoadedAddon la = byName.get(k);
            try {
                la.setExtension(instantiate(la));
                la.extension().onLoad(api);
            } catch (Throwable t) {
                Logger.severe("Addon " + la.conf().name() + " failed in onLoad: " + t);
                t.printStackTrace();
                la.markFailed(t);
            }
        }

        // Stage 2: onEnable in dependency order.
        for (String k : order) {
            LoadedAddon la = byName.get(k);
            if (la.status() == AddonStatus.FAILED) {
                addons.put(k, la);
                continue;
            }
            try {
                la.extension().onEnable(api);
                la.markEnabled();
                Logger.info("Enabled addon: " + la.conf().name()
                        + " v" + la.conf().version()
                        + (la.conf().targetApiVersion() == null
                            ? ""
                            : " (built against API " + la.conf().targetApiVersion() + ")"));
            } catch (Throwable t) {
                Logger.severe("Addon " + la.conf().name() + " failed in onEnable: " + t);
                t.printStackTrace();
                la.markFailed(t);
                rollbackRegistrations(la);
            }
            addons.put(k, la);
        }
    }

    /**
     * Reflectively instantiate the addon's main class and verify it implements
     * MenuExtension.
     */
    private ru.abstractmenus.api.MenuExtension instantiate(LoadedAddon la) throws Exception {
        Class<?> main = la.classLoader().loadClass(la.conf().main());
        if (!ru.abstractmenus.api.MenuExtension.class.isAssignableFrom(main)) {
            throw new IllegalStateException("main class " + main.getName()
                    + " does not implement MenuExtension");
        }
        return (ru.abstractmenus.api.MenuExtension) main.getDeclaredConstructor().newInstance();
    }

    /** Strip any type registrations the failed addon managed to make. */
    private void rollbackRegistrations(LoadedAddon la) {
        if (la.extension() == null) return;
        api.actions().unregisterAll(la.extension());
        api.rules().unregisterAll(la.extension());
        api.activators().unregisterAll(la.extension());
        api.itemProperties().unregisterAll(la.extension());
        api.catalogs().unregisterAll(la.extension());
    }

    /**
     * Disable every loaded addon (in reverse enable order) and release
     * classloader resources. Called from plugin onDisable.
     */
    public void unloadAll() {
        // Disable in reverse enable order.
        var reversed = new java.util.ArrayList<>(addons.values());
        java.util.Collections.reverse(reversed);
        for (LoadedAddon la : reversed) {
            try {
                if (la.status() == AddonStatus.ENABLED && la.extension() != null) {
                    la.extension().onDisable(api);
                }
                rollbackRegistrations(la);
                la.markDisabled();
            } catch (Throwable t) {
                Logger.severe("Addon " + la.conf().name() + " failed in onDisable: " + t);
                t.printStackTrace();
                // Don't let one bad disable block the others.
            }
            try {
                la.classLoader().close();
            } catch (Exception e) {
                Logger.warning("Addon " + la.conf().name() + " classloader close failed: " + e);
            }
        }
        addons.clear();
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
        String key = name.toLowerCase();
        LoadedAddon existing = addons.get(key);
        if (existing == null) return Optional.empty();

        // Disable + unhook current instance.
        try {
            if (existing.status() == AddonStatus.ENABLED && existing.extension() != null) {
                existing.extension().onDisable(api);
            }
            rollbackRegistrations(existing);
        } catch (Throwable t) {
            Logger.warning("Addon " + existing.conf().name()
                    + " failed in onDisable during reload: " + t);
        }
        try { existing.classLoader().close(); } catch (Exception ignored) {}
        addons.remove(key);

        // Re-discover: find the jar whose addon.conf name matches.
        Path freshJar = findJarByName(name);
        if (freshJar == null) {
            Logger.warning("Addon " + name + " jar no longer present — not reloaded");
            return Optional.empty();
        }

        LoadedAddon fresh;
        try {
            fresh = readAddonJar(freshJar);
        } catch (Exception e) {
            Logger.severe("Addon " + name + " jar failed to re-parse: " + e.getMessage());
            return Optional.empty();
        }

        // Enable the single addon. We don't re-chain the full topological sort
        // for a single-addon reload — assume its addonDependencies are already
        // enabled (they were, before this reload).
        try {
            fresh.setExtension(instantiate(fresh));
            fresh.extension().onLoad(api);
            fresh.extension().onEnable(api);
            fresh.markEnabled();
            addons.put(fresh.conf().name().toLowerCase(), fresh);
            Logger.info("Reloaded addon: " + fresh.conf().name() + " v" + fresh.conf().version());
        } catch (Throwable t) {
            Logger.severe("Addon " + name + " failed during reload: " + t);
            t.printStackTrace();
            fresh.markFailed(t);
            rollbackRegistrations(fresh);
            addons.put(fresh.conf().name().toLowerCase(), fresh);
        }

        return Optional.of(fresh);
    }

    /** Scan addonsDir again, return the first jar whose addon.conf.name matches. */
    private Path findJarByName(String name) {
        if (!java.nio.file.Files.isDirectory(addonsDir)) return null;
        try (var stream = java.nio.file.Files.newDirectoryStream(addonsDir, "*.jar")) {
            for (Path jar : stream) {
                try (var jf = new java.util.jar.JarFile(jar.toFile())) {
                    var entry = jf.getJarEntry("addon.conf");
                    if (entry == null) continue;
                    String hocon = new String(jf.getInputStream(entry).readAllBytes(),
                            java.nio.charset.StandardCharsets.UTF_8);
                    AddonConf c = AddonConf.parse(hocon);
                    if (c.name().equalsIgnoreCase(name)) return jar;
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
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

}
