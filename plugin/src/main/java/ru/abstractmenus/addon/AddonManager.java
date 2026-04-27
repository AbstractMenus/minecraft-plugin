package ru.abstractmenus.addon;

import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
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
     * Test-only overload: inject addonsDir directly, skip Bukkit plugin-dep
     * checks (since no {@code plugin.getServer()} is available in pure-unit
     * tests). Any addon with a non-empty {@code pluginDependencies} will fail
     * under this constructor.
     */
    AddonManager(Path addonsDir, AbstractMenusApi api) {
        this.plugin = null;
        this.api = api;
        this.addonsDir = addonsDir;
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
        // In test mode (plugin == null), skip this check — tests must not declare
        // pluginDependencies.
        var byName = new LinkedHashMap<String, LoadedAddon>();
        if (plugin != null) {
            var pluginManager = plugin.getServer().getPluginManager();
            for (LoadedAddon la : pending.values()) {
                AddonConf c = la.getConf();
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
        } else {
            byName.putAll(pending);
        }

        if (byName.isEmpty()) return;

        // Sort by addon-level dependencies.
        Map<String, List<String>> depGraph = new LinkedHashMap<>();
        for (var e : byName.entrySet()) {
            List<String> deps = e.getValue().getConf().addonDependencies().stream()
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
                addons.put(la.getConf().name().toLowerCase(), la);
            }
            return;
        }

        // Stage 1: onLoad for all — ordering-independent setup.
        for (String k : order) {
            LoadedAddon la = byName.get(k);
            try {
                la.setExtension(instantiate(la));
                la.getExtension().onLoad(api);
            } catch (Throwable t) {
                Logger.severe("Addon " + la.getConf().name() + " failed in onLoad: " + t);
                t.printStackTrace();
                la.markFailed(t);
            }
        }

        // Stage 2: onEnable in dependency order.
        for (String k : order) {
            LoadedAddon la = byName.get(k);
            if (la.getStatus() == AddonStatus.FAILED) {
                addons.put(k, la);
                continue;
            }
            try {
                la.getExtension().onEnable(api);
                la.markEnabled();
                Logger.info("Enabled addon: " + la.getConf().name()
                        + " v" + la.getConf().version()
                        + (la.getConf().targetApiVersion() == null
                            ? ""
                            : " (built against API " + la.getConf().targetApiVersion() + ")"));
            } catch (Throwable t) {
                Logger.severe("Addon " + la.getConf().name() + " failed in onEnable: " + t);
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
        Class<?> main = la.getClassLoader().loadClass(la.getConf().main());
        if (!ru.abstractmenus.api.MenuExtension.class.isAssignableFrom(main)) {
            throw new IllegalStateException("main class " + main.getName()
                    + " does not implement MenuExtension");
        }
        return (ru.abstractmenus.api.MenuExtension) main.getDeclaredConstructor().newInstance();
    }

    /** Strip any type registrations the failed addon managed to make. */
    private void rollbackRegistrations(LoadedAddon la) {
        if (la.getExtension() == null) return;
        api.actions().unregisterAll(la.getExtension());
        api.rules().unregisterAll(la.getExtension());
        api.activators().unregisterAll(la.getExtension());
        api.itemProperties().unregisterAll(la.getExtension());
        api.catalogs().unregisterAll(la.getExtension());
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
                if (la.getStatus() == AddonStatus.ENABLED && la.getExtension() != null) {
                    la.getExtension().onDisable(api);
                }
                rollbackRegistrations(la);
                la.markDisabled();
            } catch (Throwable t) {
                Logger.severe("Addon " + la.getConf().name() + " failed in onDisable: " + t);
                t.printStackTrace();
                // Don't let one bad disable block the others.
            }
            try {
                la.getClassLoader().close();
            } catch (Exception e) {
                Logger.warning("Addon " + la.getConf().name() + " classloader close failed: " + e);
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
            if (existing.getStatus() == AddonStatus.ENABLED && existing.getExtension() != null) {
                existing.getExtension().onDisable(api);
            }
            rollbackRegistrations(existing);
        } catch (Throwable t) {
            Logger.warning("Addon " + existing.getConf().name()
                    + " failed in onDisable during reload: " + t);
        }
        try { existing.getClassLoader().close(); } catch (Exception ignored) {}
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
            fresh.getExtension().onLoad(api);
            fresh.getExtension().onEnable(api);
            fresh.markEnabled();
            addons.put(fresh.getConf().name().toLowerCase(), fresh);
            Logger.info("Reloaded addon: " + fresh.getConf().name() + " v" + fresh.getConf().version());
        } catch (Throwable t) {
            Logger.severe("Addon " + name + " failed during reload: " + t);
            t.printStackTrace();
            fresh.markFailed(t);
            rollbackRegistrations(fresh);
            addons.put(fresh.getConf().name().toLowerCase(), fresh);
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
                    String key = addon.getConf().name().toLowerCase();
                    if (pending.containsKey(key)) {
                        Logger.warning("Duplicate addon name '" + addon.getConf().name()
                                + "' — ignoring " + jar.getFileName());
                        try { addon.getClassLoader().close(); } catch (Exception ignored) {}
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
     * Load a single addon by its addon.conf {@code name} from the addons
     * directory. Useful when the operator drops one new jar at runtime
     * and runs {@code /am addons load <name>} - no need to bounce the
     * server to discover it.
     *
     * <p>Returns {@code Optional.empty()} if no jar with a matching
     * {@code addon.conf name} is found, or if an addon with that name is
     * already in the loaded map. Otherwise returns the {@link LoadedAddon}
     * (which may be ENABLED or FAILED depending on what happened).
     *
     * @param name the addon-conf {@code name}, case-insensitive
     */
    public Optional<LoadedAddon> loadOne(String name) {
        if (addons.containsKey(name.toLowerCase())) {
            return Optional.empty();
        }
        Path jar = findJarByName(name);
        if (jar == null) return Optional.empty();

        LoadedAddon la;
        try {
            la = readAddonJar(jar);
        } catch (Exception e) {
            Logger.severe("Addon " + name + " failed to parse: " + e.getMessage());
            return Optional.empty();
        }
        enableSingle(la);
        return Optional.of(la);
    }

    /**
     * Re-scan the addons directory and load every addon not already in
     * the loaded map. Existing addons are left alone (their classloader
     * is not rebuilt - use {@link #reload(String)} for that). Returns
     * the list of addons that were attempted in this call (some may
     * have ended up in FAILED state).
     */
    public List<LoadedAddon> rescan() {
        List<LoadedAddon> newlyLoaded = new ArrayList<>();
        Map<String, LoadedAddon> discovered = discover();
        for (var entry : discovered.entrySet()) {
            LoadedAddon la = entry.getValue();
            if (addons.containsKey(entry.getKey())) {
                // Already loaded - drop the redundant classloader we just built.
                try { la.getClassLoader().close(); } catch (Exception ignored) {}
                continue;
            }
            enableSingle(la);
            newlyLoaded.add(la);
        }
        return newlyLoaded;
    }

    /**
     * Return addon-conf {@code name}s found on disk under the addons
     * directory but not yet loaded into memory. Used by tab completion
     * for {@code /am addons load <name>}. Cost is one jar open and one
     * HOCON parse per .jar in the directory - acceptable at typical
     * scale (1-20 addons), but be aware this is not free.
     */
    public List<String> availableNotLoaded() {
        if (!java.nio.file.Files.isDirectory(addonsDir)) return List.of();
        List<String> result = new ArrayList<>();
        try (var stream = java.nio.file.Files.newDirectoryStream(addonsDir, "*.jar")) {
            for (Path jar : stream) {
                try (var jf = new java.util.jar.JarFile(jar.toFile())) {
                    var entry = jf.getJarEntry("addon.conf");
                    if (entry == null) continue;
                    String hocon = new String(jf.getInputStream(entry).readAllBytes(),
                            java.nio.charset.StandardCharsets.UTF_8);
                    AddonConf conf = AddonConf.parse(hocon);
                    if (!addons.containsKey(conf.name().toLowerCase())) {
                        result.add(conf.name());
                    }
                } catch (Exception ignored) {
                    // Malformed jar - skip silently, the operator already saw
                    // the warning at server-start discover() time.
                }
            }
        } catch (Exception ignored) {}
        return result;
    }

    /**
     * Verify Bukkit-side and addon-side dependencies, then run
     * onLoad + onEnable. Installs the result into the loaded map
     * (regardless of success or failure - failed addons stay visible
     * in {@code /am addons list} so the operator can debug them).
     */
    private void enableSingle(LoadedAddon la) {
        String key = la.getConf().name().toLowerCase();

        if (plugin != null) {
            var pm = plugin.getServer().getPluginManager();
            for (String dep : la.getConf().pluginDependencies()) {
                if (pm.getPlugin(dep) == null) {
                    String msg = "missing plugin dependency: " + dep;
                    Logger.warning("Addon " + la.getConf().name() + " " + msg);
                    la.markFailed(new IllegalStateException(msg));
                    addons.put(key, la);
                    return;
                }
            }
        }

        for (String dep : la.getConf().addonDependencies()) {
            LoadedAddon depAddon = addons.get(dep.toLowerCase());
            if (depAddon == null || depAddon.getStatus() != AddonStatus.ENABLED) {
                String msg = "missing or unhealthy addon dependency: " + dep;
                Logger.warning("Addon " + la.getConf().name() + " " + msg);
                la.markFailed(new IllegalStateException(msg));
                addons.put(key, la);
                return;
            }
        }

        try {
            la.setExtension(instantiate(la));
            la.getExtension().onLoad(api);
            la.getExtension().onEnable(api);
            la.markEnabled();
            Logger.info("Enabled addon: " + la.getConf().name()
                    + " v" + la.getConf().version()
                    + (la.getConf().targetApiVersion() == null ? ""
                        : " (built against API " + la.getConf().targetApiVersion() + ")"));
        } catch (Throwable t) {
            Logger.severe("Addon " + la.getConf().name() + " failed during enable: " + t);
            t.printStackTrace();
            la.markFailed(t);
            rollbackRegistrations(la);
        }
        addons.put(key, la);
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
        ClassLoader parent = (plugin != null)
                ? plugin.getClass().getClassLoader()
                : AddonManager.class.getClassLoader();
        AddonClassLoader cl = new AddonClassLoader(
                new java.net.URL[]{jarPath.toUri().toURL()},
                parent);

        return new LoadedAddon(conf, cl);
    }

}
