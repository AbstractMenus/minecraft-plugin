package ru.abstractmenus.impl;

import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.api.TypeRegistry;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.hocon.api.serialize.NodeSerializers;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * In-memory {@link TypeRegistry} implementation. Not thread-safe for
 * concurrent registration; consumers must register/unregister from the main
 * server thread.
 *
 * <p>Note on {@code NodeSerializers.unregister}: hocon 1.0.6 does NOT expose
 * an {@code unregister(Class)} method. We therefore reach into its private
 * backing map via reflection (one-time {@link Field} lookup, cached) so
 * {@link #unregisterAll(MenuExtension)} can drop stale {@code Class} keys.
 * Without this the {@code Class} object keeps the addon's now-closed
 * classloader alive forever (native FDs, jar handle, all loaded classes).
 */
public final class TypeRegistryImpl<T> implements TypeRegistry<T> {

    private static final Logger LOG = Logger.getLogger(TypeRegistryImpl.class.getName());

    /**
     * Reflective handle to {@link NodeSerializers}'s private
     * {@code serializers} map field. Resolved once at class init; if hocon
     * ever renames it, we log a warning and fall through to the harmless
     * "leave the entry" behaviour.
     */
    private static final Field NODE_SERIALIZERS_MAP_FIELD;
    static {
        Field f = null;
        try {
            f = NodeSerializers.class.getDeclaredField("serializers");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            LOG.log(Level.WARNING,
                    "NodeSerializers.serializers field not found - bundled hocon lib has "
                            + "changed shape since AbstractMenus was built. Not an AbstractMenus "
                            + "bug; needs an upstream hocon change to expose unregister(Class). "
                            + "Side effect: every addon disable/reload from now on leaks its "
                            + "classloader until a full server restart.",
                    e);
        }
        NODE_SERIALIZERS_MAP_FIELD = f;
    }

    private final NodeSerializers serializers;

    /** key (lowercased) → registered class */
    private final Map<String, Class<? extends T>> byKey = new HashMap<>();

    /** class → key (reverse index, kept in sync with {@link #byKey}) */
    private final Map<Class<? extends T>, String> byType = new IdentityHashMap<>();

    /** owner → set of keys they registered (for unregisterAll) */
    private final Map<MenuExtension, Set<String>> keysByOwner = new IdentityHashMap<>();

    public TypeRegistryImpl(NodeSerializers serializers) {
        this.serializers = serializers;
    }

    @Override
    public synchronized <S extends T> void register(String key,
                                                    Class<S> type,
                                                    NodeSerializer<S> serializer,
                                                    MenuExtension owner) {
        String k = key.toLowerCase();

        Class<? extends T> existing = byKey.get(k);
        if (existing != null) {
            LOG.warning("TypeRegistry: overwriting existing entry '" + k
                    + "' (" + existing.getName() + " -> " + type.getName() + ")");
            byType.remove(existing);
            // Strip k from the old owner's set so their later unregisterAll
            // doesn't wipe the new owner's entry. Without this the *new*
            // owner's class would silently vanish from the registry the
            // first time the *old* owner gets disabled.
            for (Set<String> ownerKeys : keysByOwner.values()) {
                ownerKeys.remove(k);
            }
        }

        byKey.put(k, type);
        byType.put(type, k);
        serializers.register(type, serializer);

        keysByOwner.computeIfAbsent(owner, o -> new HashSet<>()).add(k);
    }

    @Override
    public synchronized Class<? extends T> get(String key) {
        return byKey.get(key.toLowerCase());
    }

    @Override
    public synchronized String name(Class<? extends T> type) {
        return byType.get(type);
    }

    @Override
    public synchronized Set<String> keys() {
        return Collections.unmodifiableSet(new HashSet<>(byKey.keySet()));
    }

    /**
     * Snapshot of every extension that has ever registered (and not since
     * fully unregistered) at least one entry in this registry. Used by
     * {@code /am addons list} to surface Path 1 plugin-as-addons that
     * don't sit in the AddonManager's loaded map.
     */
    public synchronized Set<MenuExtension> seenOwners() {
        return Collections.unmodifiableSet(new HashSet<>(keysByOwner.keySet()));
    }

    /**
     * Wipe every entry registered by {@code owner}. Intentionally NOT on the
     * public {@link TypeRegistry} interface so that addons cannot use it to
     * unregister another extension's entries. Called only by AbstractMenus'
     * internal addon manager via a cast on the impl reference.
     */
    public synchronized void unregisterAll(MenuExtension owner) {
        Set<String> keys = keysByOwner.remove(owner);
        if (keys == null) return;

        for (String k : keys) {
            Class<? extends T> type = byKey.remove(k);
            if (type != null) {
                byType.remove(type);
                removeSerializerEntry(type);
            }
        }
    }

    /**
     * Drop a {@code Class -> NodeSerializer} entry from the backing
     * {@link NodeSerializers}. Done via reflection because hocon 1.0.6 does
     * not expose an unregister method. Failure is non-fatal: we log and
     * leave the entry, accepting the classloader-leak cost rather than
     * crashing the disable path.
     */
    private void removeSerializerEntry(Class<?> type) {
        if (NODE_SERIALIZERS_MAP_FIELD == null) return;
        try {
            @SuppressWarnings("unchecked")
            Map<Class<?>, NodeSerializer<?>> backing =
                    (Map<Class<?>, NodeSerializer<?>>) NODE_SERIALIZERS_MAP_FIELD.get(serializers);
            backing.remove(type);
        } catch (Throwable t) {
            LOG.log(Level.WARNING,
                    "Cannot remove NodeSerializer for " + type.getName()
                            + ": bundled hocon lib has no public unregister(Class), and "
                            + "our reflection workaround failed. Not an AbstractMenus bug - "
                            + "needs an upstream hocon change. Side effect: this addon's "
                            + "classloader stays in memory until a full server restart; the "
                            + "leak compounds across reloads.",
                    t);
        }
    }
}
