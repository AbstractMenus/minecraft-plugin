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

    // package-private helpers filled in by subsequent tasks (discover, readAddonJar,
    // instantiate, rollbackRegistrations, findJarByName)
}
