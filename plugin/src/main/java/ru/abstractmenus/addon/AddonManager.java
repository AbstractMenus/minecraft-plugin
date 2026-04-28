package ru.abstractmenus.addon;

import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.impl.ProviderRegistryImpl;
import ru.abstractmenus.impl.TypeRegistryImpl;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Loads, enables, and manages AM-loaded addons (the lightweight jars in
 * {@code plugins/AbstractMenus/addons/}).
 *
 * <p>Plugin-as-addons (Path 1) are not handled here — they boot through
 * Bukkit's own plugin lifecycle and look up the API via
 * {@link AbstractMenusApi#get()}.
 */
public final class AddonManager {

    /**
     * TTL for the availableNotLoaded() cache. Tab completion fires on every
     * keystroke; without a cache that is N jar opens + N HOCON parses on the
     * main thread per TAB. 2 seconds is short enough that the operator does
     * not see staleness in practice (drop a jar, wait a beat, hit TAB).
     */
    private static final long AVAILABLE_CACHE_TTL_MS = 2_000L;

    /**
     * Hard cap on the size of {@code addon.conf} read from a jar. A real
     * addon.conf is well under a kilobyte; refusing to read more than 64 KB
     * defends against a malicious or corrupted jar declaring a huge
     * uncompressed size that would OOM the server when {@code readAllBytes}
     * tries to allocate the buffer.
     */
    private static final int MAX_ADDON_CONF_BYTES = 64 * 1024;

    private final Path addonsDir;
    private final AbstractMenusApi api;
    private final PluginDepChecker depChecker;
    private final ClassLoader parentClassLoader;

    /** name (lowercased) → LoadedAddon, insertion-ordered (matches enable order) */
    private final Map<String, LoadedAddon> addons = new LinkedHashMap<>();

    private volatile List<String> cachedAvailable = List.of();
    private volatile long cachedAvailableAt = 0L;

    public AddonManager(AbstractMenus plugin, AbstractMenusApi api) {
        this.api = api;
        this.addonsDir = plugin.getDataFolder().toPath().resolve("addons");
        this.depChecker = name -> plugin.getServer().getPluginManager().getPlugin(name) != null;
        this.parentClassLoader = plugin.getClass().getClassLoader();
    }

    /**
     * Test-only overload: inject addonsDir directly. Bukkit-plugin
     * dependency checks are stubbed to always report present so a test
     * addon can declare {@code pluginDependencies} without booting a
     * server.
     */
    AddonManager(Path addonsDir, AbstractMenusApi api) {
        this.api = api;
        this.addonsDir = addonsDir;
        this.depChecker = PluginDepChecker.ALL_PRESENT;
        this.parentClassLoader = AddonManager.class.getClassLoader();
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

        // Filter out addons whose hard plugin deps are missing. Soft deps
        // log a warning but do not block the addon.
        var byName = new LinkedHashMap<String, LoadedAddon>();
        for (LoadedAddon la : pending.values()) {
            if (checkPluginDeps(la)) {
                byName.put(la.getConf().name().toLowerCase(), la);
            }
        }

        if (byName.isEmpty()) return;

        // Build the addon-dep graph.
        Map<String, List<String>> depGraph = new LinkedHashMap<>();
        for (var e : byName.entrySet()) {
            List<String> deps = e.getValue().getConf().addonDependencies().stream()
                    .map(String::toLowerCase).toList();
            depGraph.put(e.getKey(), deps);
        }

        // Pre-filter addons whose deps point to names not in the graph -
        // typically because the dep is a plugin-as-addon (Path 1) which
        // doesn't show up in the addons/ folder, or simply a typo. Mark
        // them FAILED individually instead of poisoning the whole batch
        // through topoSort. Runs to fixed point so transitive failures
        // (A -> B -> missing C) catch both A and B in the same pass.
        Set<String> unsatisfied = AddonDependencyGraph.unsatisfied(depGraph);
        Set<String> originalGraphKeys = Set.copyOf(depGraph.keySet());
        for (String key : unsatisfied) {
            LoadedAddon la = byName.remove(key);
            depGraph.remove(key);
            String missing = la.getConf().addonDependencies().stream()
                    .filter(d -> !originalGraphKeys.contains(d.toLowerCase())
                            || unsatisfied.contains(d.toLowerCase()))
                    .findFirst().orElse("?");
            String msg = "missing addon dependency: " + missing;
            Logger.warning("Addon " + la.getConf().name() + " " + msg + " - skipping");
            la.markFailed(new IllegalStateException(msg));
            addons.put(key, la);
        }

        if (byName.isEmpty()) return;

        List<String> order;
        try {
            order = AddonDependencyGraph.topoSort(depGraph);
        } catch (AddonDependencyCycleException ex) {
            Logger.severe("Addon dependency cycle detected: " + ex.getMessage());
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
                // onLoad shouldn't register types per the contract, but a
                // misbehaving addon might have done so before throwing -
                // strip whatever it managed so the next addon's enable
                // sees a clean registry state.
                rollbackRegistrations(la);
            }
        }

        // Stage 2: onEnable in dependency order.
        for (String k : order) {
            LoadedAddon la = byName.get(k);
            // Skip both FAILED entries and the defensive case where Stage 1
            // somehow returned without an extension instance - either way,
            // calling onEnable on a null extension would NPE.
            if (la.getStatus() == AddonStatus.FAILED || la.getExtension() == null) {
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
    private MenuExtension instantiate(LoadedAddon la) throws Exception {
        Class<?> main = la.getClassLoader().loadClass(la.getConf().main());
        if (!MenuExtension.class.isAssignableFrom(main)) {
            throw new IllegalStateException("main class " + main.getName()
                    + " does not implement MenuExtension");
        }
        return (MenuExtension) main.getDeclaredConstructor().newInstance();
    }

    /**
     * Strip any type registrations the failed addon managed to make.
     *
     * <p>Casts each registry to its {@code Impl} because {@code unregisterAll}
     * is intentionally not on the public {@link ru.abstractmenus.api.TypeRegistry}
     * / {@link ru.abstractmenus.api.ProviderRegistry} interfaces - addons
     * shouldn't be able to wipe each other's registrations.
     */
    private void rollbackRegistrations(LoadedAddon la) {
        if (la.getExtension() == null) return;
        MenuExtension ext = la.getExtension();
        ((TypeRegistryImpl<?>) api.actions()).unregisterAll(ext);
        ((TypeRegistryImpl<?>) api.rules()).unregisterAll(ext);
        ((TypeRegistryImpl<?>) api.activators()).unregisterAll(ext);
        ((TypeRegistryImpl<?>) api.itemProperties()).unregisterAll(ext);
        ((TypeRegistryImpl<?>) api.catalogs()).unregisterAll(ext);
        ((ProviderRegistryImpl) api.providers()).unregisterAll(ext);
    }

    /**
     * Disable every loaded addon (in reverse enable order) and release
     * classloader resources. Called from plugin onDisable.
     */
    public void unloadAll() {
        // Disable in reverse enable order.
        var reversed = new ArrayList<>(addons.values());
        Collections.reverse(reversed);
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
     * <p>Goes through {@link #enableSingle} so plugin-dep and addon-dep
     * checks re-run; a dependency that disappeared between the original
     * load and this reload causes a clean FAILED state instead of a
     * confusing trace deep inside {@code onEnable}.
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
        cachedAvailableAt = 0L;

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

        enableSingle(fresh);
        return Optional.of(fresh);
    }

    /** Scan addonsDir again, return the first jar whose addon.conf.name matches. */
    private Path findJarByName(String name) {
        return scanJarConfs().entrySet().stream()
                .filter(e -> e.getValue().name().equalsIgnoreCase(name))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public Collection<LoadedAddon> loaded() {
        return Collections.unmodifiableCollection(addons.values());
    }

    public Optional<LoadedAddon> get(String name) {
        return Optional.ofNullable(addons.get(name.toLowerCase()));
    }

    /**
     * Every {@link MenuExtension} that has registered at least one type or
     * provider on the running API. Includes:
     * <ul>
     *   <li>Path 2 AM-loaded addons (also in {@link #loaded()})</li>
     *   <li>Path 1 plugin-as-addons (NOT in {@link #loaded()} — they live
     *       on Bukkit's plugin lifecycle, we only see them through their
     *       registry footprint)</li>
     *   <li>Plugin-internal extensions like {@code CoreExtension}</li>
     * </ul>
     *
     * <p>Used by {@code /am addons list} to surface Path 1 entries that
     * would otherwise be invisible under {@code /am addons}.
     */
    public Set<MenuExtension> knownExtensions() {
        Set<MenuExtension> all = new HashSet<>();
        all.addAll(((TypeRegistryImpl<?>) api.actions()).seenOwners());
        all.addAll(((TypeRegistryImpl<?>) api.rules()).seenOwners());
        all.addAll(((TypeRegistryImpl<?>) api.activators()).seenOwners());
        all.addAll(((TypeRegistryImpl<?>) api.itemProperties()).seenOwners());
        all.addAll(((TypeRegistryImpl<?>) api.catalogs()).seenOwners());
        all.addAll(((ProviderRegistryImpl) api.providers()).seenOwners());
        return all;
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

        if (!Files.isDirectory(addonsDir)) {
            try {
                Files.createDirectories(addonsDir);
            } catch (IOException e) {
                Logger.warning("Could not create addons directory " + addonsDir + ": " + e.getMessage());
            }
            return pending;
        }

        try (var stream = Files.newDirectoryStream(addonsDir, "*.jar")) {
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
        } catch (IOException e) {
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
        cachedAvailableAt = 0L;
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
        if (!newlyLoaded.isEmpty()) cachedAvailableAt = 0L;
        return newlyLoaded;
    }

    /**
     * Return addon-conf {@code name}s found on disk under the addons
     * directory but not yet loaded into memory. Used by tab completion
     * for {@code /am addons load <name>}.
     *
     * <p>Result is cached for {@value #AVAILABLE_CACHE_TTL_MS} ms because
     * tab completion runs synchronously on the main thread and a cold call
     * does one jar open + HOCON parse per *.jar in the addons folder.
     */
    public List<String> availableNotLoaded() {
        long now = System.currentTimeMillis();
        if (now - cachedAvailableAt < AVAILABLE_CACHE_TTL_MS) {
            return cachedAvailable;
        }
        List<String> result = scanJarConfs().values().stream()
                .map(AddonConf::name)
                .filter(n -> !addons.containsKey(n.toLowerCase()))
                .toList();
        cachedAvailable = result;
        cachedAvailableAt = now;
        return result;
    }

    /**
     * One canonical pass over {@code addonsDir}: for each {@code *.jar},
     * read its {@code addon.conf} entry and parse it. Entries that lack
     * addon.conf or fail to parse are skipped silently (the operator
     * already saw the warning at startup discover() time).
     *
     * <p>Sole shared helper for {@link #findJarByName} and
     * {@link #availableNotLoaded} to avoid duplicating the open-read-parse
     * triple. Note that {@link #discover()} is separate because it ALSO
     * builds the {@link AddonClassLoader}, which we don't want for the
     * tab-completion path.
     *
     * @return jar Path → parsed AddonConf, in directory iteration order
     */
    private Map<Path, AddonConf> scanJarConfs() {
        if (!Files.isDirectory(addonsDir)) return Map.of();
        Map<Path, AddonConf> result = new LinkedHashMap<>();
        try (var stream = Files.newDirectoryStream(addonsDir, "*.jar")) {
            for (Path jar : stream) {
                try (var jf = new JarFile(jar.toFile())) {
                    JarEntry entry = jf.getJarEntry("addon.conf");
                    if (entry == null) continue;
                    result.put(jar, AddonConf.parse(readBoundedAddonConf(jf, entry)));
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return result;
    }

    /**
     * Read {@code addon.conf} into a String, refusing entries larger than
     * {@link #MAX_ADDON_CONF_BYTES}. Both the declared uncompressed size
     * (zip header) and the actual byte count are checked so a crafted
     * jar can't lie about either one.
     */
    private static String readBoundedAddonConf(JarFile jar, JarEntry entry) throws IOException {
        long declared = entry.getSize();
        if (declared > MAX_ADDON_CONF_BYTES) {
            throw new IOException("addon.conf too large (" + declared + " bytes, cap "
                    + MAX_ADDON_CONF_BYTES + ")");
        }
        try (var in = jar.getInputStream(entry)) {
            byte[] bytes = in.readNBytes(MAX_ADDON_CONF_BYTES + 1);
            if (bytes.length > MAX_ADDON_CONF_BYTES) {
                throw new IOException("addon.conf exceeded " + MAX_ADDON_CONF_BYTES
                        + " bytes during read (declared size " + declared + ")");
            }
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * Verify hard {@code pluginDependencies} are present and log warnings
     * for any missing {@code pluginSoftDependencies}.
     *
     * <p>If a hard dep is missing the addon is marked FAILED and parked
     * in the loaded map (so {@code /am addons list} surfaces it) and the
     * method returns false. Soft-dep misses log a warning but do not
     * block enabling.
     *
     * @return true if the addon may proceed to enable, false if a hard
     *         dependency is missing
     */
    private boolean checkPluginDeps(LoadedAddon la) {
        AddonConf c = la.getConf();
        String key = c.name().toLowerCase();

        for (String dep : c.pluginDependencies()) {
            if (!depChecker.isPresent(dep)) {
                String msg = "missing plugin dependency: " + dep;
                Logger.warning("Addon " + c.name() + " " + msg + " - skipping");
                la.markFailed(new IllegalStateException(msg));
                addons.put(key, la);
                return false;
            }
        }

        for (String dep : c.pluginSoftDependencies()) {
            if (!depChecker.isPresent(dep)) {
                Logger.warning("Addon " + c.name()
                        + " soft-depends on plugin '" + dep
                        + "' which is not installed - features that need it may no-op");
            }
        }

        return true;
    }

    /**
     * Verify Bukkit-side and addon-side dependencies, then run
     * onLoad + onEnable. Installs the result into the loaded map
     * (regardless of success or failure - failed addons stay visible
     * in {@code /am addons list} so the operator can debug them).
     */
    private void enableSingle(LoadedAddon la) {
        String key = la.getConf().name().toLowerCase();

        if (!checkPluginDeps(la)) return;

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
    private LoadedAddon readAddonJar(Path jarPath) throws IOException {
        String hocon;
        try (var jar = new JarFile(jarPath.toFile())) {
            JarEntry entry = jar.getJarEntry("addon.conf");
            if (entry == null) {
                throw new IOException("no addon.conf at jar root");
            }
            hocon = readBoundedAddonConf(jar, entry);
        }

        AddonConf conf = AddonConf.parse(hocon);
        AddonClassLoader cl = new AddonClassLoader(
                new URL[]{jarPath.toUri().toURL()},
                parentClassLoader);

        return new LoadedAddon(conf, cl);
    }
}
