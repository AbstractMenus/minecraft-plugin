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
                    "NodeSerializers.serializers field missing; addon-disable will leak classloader references",
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
            // Intentionally do not remove from owner tracking — the old owner
            // no longer has this key since it's overwritten; cleanup of their
            // orphan entries happens on their own unregisterAll.
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
                    "Failed to drop NodeSerializers entry for " + type.getName()
                            + "; addon classloader may be retained",
                    t);
        }
    }
}
