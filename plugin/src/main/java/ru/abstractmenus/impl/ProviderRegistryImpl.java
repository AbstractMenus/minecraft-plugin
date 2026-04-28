package ru.abstractmenus.impl;

import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.api.ProviderRegistry;
import ru.abstractmenus.api.ProviderSection;
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
 * Default {@link ProviderRegistry} implementation. Five sections backed by
 * the inner {@link SectionImpl} class. Insertion-ordered per section so
 * that equal-priority ties resolve to the first-registered entry.
 *
 * <p>Sections are constructed once in this class's constructor with their
 * "kind" string ({@code "economy"}, {@code "permissions"}, ...) so each
 * one knows which {@code config.conf providers.<kind>} entry applies.
 *
 * <p>Thread-safe for registration/unregistration via per-section
 * {@code synchronized} methods; production use expects all mutation on the
 * main server thread during plugin / extension enable / disable.
 */
public final class ProviderRegistryImpl implements ProviderRegistry {

    /** section kind → configured-default id (e.g. "economy" → "playerpoints"). */
    private Function<String, String> configDefaults = kind -> null;

    private final SectionImpl<EconomyHandler> economy = new SectionImpl<>("economy", this);
    private final SectionImpl<PermissionsHandler> permissions = new SectionImpl<>("permissions", this);
    private final SectionImpl<LevelHandler> levels = new SectionImpl<>("levels", this);
    private final SectionImpl<PlaceholderHandler> placeholders = new SectionImpl<>("placeholders", this);
    private final SectionImpl<SkinHandler> skins = new SectionImpl<>("skins", this);

    /** Wire up the config-backed default source. Called once from AbstractMenusApiImpl. */
    public void setConfigDefaults(Function<String, String> lookup) {
        this.configDefaults = lookup;
    }

    @Override
    public ProviderSection<EconomyHandler> economy() {
        return economy;
    }

    @Override
    public ProviderSection<PermissionsHandler> permissions() {
        return permissions;
    }

    @Override
    public ProviderSection<LevelHandler> levels() {
        return levels;
    }

    @Override
    public ProviderSection<PlaceholderHandler> placeholders() {
        return placeholders;
    }

    @Override
    public ProviderSection<SkinHandler> skins() {
        return skins;
    }

    /**
     * Wipe every provider registered by {@code owner} across all five
     * sections. Intentionally NOT on the public {@link ProviderRegistry}
     * interface so an addon cannot wipe another extension's providers.
     * Called only by AbstractMenus' internal addon manager.
     */
    public void unregisterAll(MenuExtension owner) {
        economy.unregisterAll(owner);
        permissions.unregisterAll(owner);
        levels.unregisterAll(owner);
        placeholders.unregisterAll(owner);
        skins.unregisterAll(owner);
    }

    /**
     * Union of every extension that has registered a provider in any of
     * the five sections. Used by {@code /am addons list} to discover
     * Path 1 plugin-as-addons whose only fingerprint is in the registry
     * owner-tracking map.
     */
    public Set<MenuExtension> seenOwners() {
        Set<MenuExtension> all = new HashSet<>();
        all.addAll(economy.seenOwners());
        all.addAll(permissions.seenOwners());
        all.addAll(levels.seenOwners());
        all.addAll(placeholders.seenOwners());
        all.addAll(skins.seenOwners());
        return Collections.unmodifiableSet(all);
    }

    // -----------------------------------------------------------------
    //  SectionImpl - one instance per provider type
    // -----------------------------------------------------------------

    private static final class SectionImpl<T> implements ProviderSection<T> {

        private final String kind;
        private final ProviderRegistryImpl owner;
        private final Map<String, Entry<T>> byId = new LinkedHashMap<>();
        private final Map<MenuExtension, Set<String>> keysByExtension = new IdentityHashMap<>();

        SectionImpl(String kind, ProviderRegistryImpl owner) {
            this.kind = kind;
            this.owner = owner;
        }

        @Override
        public synchronized void register(String id, T handler, int priority, MenuExtension extOwner) {
            String k = id.toLowerCase();
            byId.put(k, new Entry<>(handler, priority));
            keysByExtension.computeIfAbsent(extOwner, o -> new HashSet<>()).add(k);
        }

        @Override
        public synchronized T resolve() {
            // configDefaults is mutated only once (during plugin startup)
            // so reading it without our lock is fine.
            String configured = owner.configDefaults.apply(kind);
            if (configured != null && !configured.equalsIgnoreCase("auto")) {
                Entry<T> e = byId.get(configured.toLowerCase());
                if (e != null) return e.handler;
            }
            Entry<T> best = null;
            for (Entry<T> e : byId.values()) {
                if (best == null || e.priority > best.priority) best = e;
            }
            return best == null ? null : best.handler;
        }

        @Override
        public synchronized T resolve(String id) {
            Entry<T> e = byId.get(id.toLowerCase());
            return e == null ? null : e.handler;
        }

        @Override
        public synchronized Collection<T> all() {
            List<T> list = new ArrayList<>(byId.size());
            for (Entry<T> e : byId.values()) list.add(e.handler);
            return Collections.unmodifiableList(list);
        }

        @Override
        public synchronized Set<String> ids() {
            // Snapshot - byId keys may mutate after this returns.
            return Collections.unmodifiableSet(new java.util.LinkedHashSet<>(byId.keySet()));
        }

        @Override
        public synchronized boolean has(String id) {
            return byId.containsKey(id.toLowerCase());
        }

        synchronized void unregisterAll(MenuExtension extOwner) {
            Set<String> keys = keysByExtension.remove(extOwner);
            if (keys == null) return;
            for (String k : keys) byId.remove(k);
        }

        synchronized Set<MenuExtension> seenOwners() {
            return new HashSet<>(keysByExtension.keySet());
        }

        private static final class Entry<T> {
            final T handler;
            final int priority;
            Entry(T handler, int priority) { this.handler = handler; this.priority = priority; }
        }
    }
}
