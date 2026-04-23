package ru.abstractmenus.api;

import ru.abstractmenus.api.handler.EconomyHandler;
import ru.abstractmenus.api.handler.LevelHandler;
import ru.abstractmenus.api.handler.PermissionsHandler;
import ru.abstractmenus.api.handler.PlaceholderHandler;
import ru.abstractmenus.api.handler.SkinHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Default {@link ProviderRegistry} implementation. Five sections sharing an
 * inner generic {@link Section} class. Insertion-ordered per section so that
 * equal-priority ties resolve to the first-registered entry.
 *
 * <p>Thread-safe for registration/unregistration via per-section synchronized
 * methods, but production use expects all mutation to happen on the main
 * server thread during plugin / extension enable/disable.
 */
public final class ProviderRegistryImpl implements ProviderRegistry {

    private final Section<EconomyHandler>     economy      = new Section<>();
    private final Section<PermissionsHandler> permissions  = new Section<>();
    private final Section<LevelHandler>       levels       = new Section<>();
    private final Section<PlaceholderHandler> placeholders = new Section<>();
    private final Section<SkinHandler>        skins        = new Section<>();

    /** section kind → configured-default id (e.g. "economy" → "playerpoints"). */
    private Function<String, String> configDefaults = kind -> null;  // no-op by default

    /** Wire up the config-backed default source. Called once from AbstractMenusApiImpl. */
    public void setConfigDefaults(Function<String, String> lookup) {
        this.configDefaults = lookup;
    }

    // ---- Config-default resolution helper --------------------------------

    private <T> T resolveWithConfig(String kind, Section<T> section) {
        String configured = configDefaults.apply(kind);
        if (configured != null && !configured.equalsIgnoreCase("auto")) {
            T h = section.byId(configured);
            if (h != null) return h;
            // Configured id not found — fall back to auto.
        }
        return section.auto();
    }

    // ---- Economy ---------------------------------------------------------

    @Override public void registerEconomy(String id, EconomyHandler h, int pr, MenuExtension o) { economy.put(id, h, pr, o); }
    @Override public EconomyHandler economy()                 { return resolveWithConfig("economy", economy); }
    @Override public EconomyHandler economy(String id)        { return economy.byId(id); }
    @Override public Collection<EconomyHandler> allEconomy()  { return economy.all(); }
    @Override public boolean hasEconomy(String id)            { return economy.has(id); }

    // ---- Permissions -----------------------------------------------------

    @Override public void registerPermissions(String id, PermissionsHandler h, int pr, MenuExtension o) { permissions.put(id, h, pr, o); }
    @Override public PermissionsHandler permissions()                    { return resolveWithConfig("permissions", permissions); }
    @Override public PermissionsHandler permissions(String id)           { return permissions.byId(id); }
    @Override public Collection<PermissionsHandler> allPermissions()     { return permissions.all(); }
    @Override public boolean hasPermissions(String id)                   { return permissions.has(id); }

    // ---- Levels ----------------------------------------------------------

    @Override public void registerLevels(String id, LevelHandler h, int pr, MenuExtension o) { levels.put(id, h, pr, o); }
    @Override public LevelHandler levels()                 { return resolveWithConfig("levels", levels); }
    @Override public LevelHandler levels(String id)        { return levels.byId(id); }
    @Override public Collection<LevelHandler> allLevels()  { return levels.all(); }
    @Override public boolean hasLevels(String id)          { return levels.has(id); }

    // ---- Placeholders ----------------------------------------------------

    @Override public void registerPlaceholders(String id, PlaceholderHandler h, int pr, MenuExtension o) { placeholders.put(id, h, pr, o); }
    @Override public PlaceholderHandler placeholders()                    { return resolveWithConfig("placeholders", placeholders); }
    @Override public PlaceholderHandler placeholders(String id)           { return placeholders.byId(id); }
    @Override public Collection<PlaceholderHandler> allPlaceholders()     { return placeholders.all(); }
    @Override public boolean hasPlaceholders(String id)                   { return placeholders.has(id); }

    // ---- Skins -----------------------------------------------------------

    @Override public void registerSkins(String id, SkinHandler h, int pr, MenuExtension o) { skins.put(id, h, pr, o); }
    @Override public SkinHandler skins()                 { return resolveWithConfig("skins", skins); }
    @Override public SkinHandler skins(String id)        { return skins.byId(id); }
    @Override public Collection<SkinHandler> allSkins()  { return skins.all(); }
    @Override public boolean hasSkins(String id)         { return skins.has(id); }

    // ---- Cleanup ---------------------------------------------------------

    @Override
    public void unregisterAll(MenuExtension owner) {
        economy.unregisterAll(owner);
        permissions.unregisterAll(owner);
        levels.unregisterAll(owner);
        placeholders.unregisterAll(owner);
        skins.unregisterAll(owner);
    }

    // ---- Inner section ---------------------------------------------------

    private static final class Section<T> {
        private final Map<String, Entry<T>> byId = new LinkedHashMap<>();
        private final Map<MenuExtension, Set<String>> keysByOwner = new IdentityHashMap<>();

        synchronized void put(String id, T handler, int priority, MenuExtension owner) {
            String k = id.toLowerCase();
            byId.put(k, new Entry<>(handler, priority));
            keysByOwner.computeIfAbsent(owner, o -> new HashSet<>()).add(k);
        }

        synchronized T byId(String id) {
            Entry<T> e = byId.get(id.toLowerCase());
            return e == null ? null : e.handler;
        }

        synchronized boolean has(String id) {
            return byId.containsKey(id.toLowerCase());
        }

        synchronized T auto() {
            Entry<T> best = null;
            for (Entry<T> e : byId.values()) {
                if (best == null || e.priority > best.priority) best = e;
            }
            return best == null ? null : best.handler;
        }

        synchronized Collection<T> all() {
            List<T> list = new ArrayList<>(byId.size());
            for (Entry<T> e : byId.values()) list.add(e.handler);
            return Collections.unmodifiableList(list);
        }

        synchronized void unregisterAll(MenuExtension owner) {
            Set<String> keys = keysByOwner.remove(owner);
            if (keys == null) return;
            for (String k : keys) byId.remove(k);
        }

        private static final class Entry<T> {
            final T handler;
            final int priority;
            Entry(T handler, int priority) { this.handler = handler; this.priority = priority; }
        }
    }
}
