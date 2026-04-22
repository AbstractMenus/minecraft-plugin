package ru.abstractmenus.api;

import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.hocon.api.serialize.NodeSerializers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * In-memory {@link TypeRegistry} implementation. Not thread-safe for
 * concurrent registration; consumers must register/unregister from the main
 * server thread.
 *
 * <p>Note on {@code NodeSerializers.unregister}: hocon 1.0.6 does NOT expose
 * an {@code unregister(Class)} method. Therefore stale serializer entries
 * survive in {@link NodeSerializers} after {@link #unregisterAll(MenuExtension)},
 * but that is harmless — the {@link #byKey} map is the authoritative lookup
 * table, and a subsequent {@link #register} call for the same class token
 * overwrites the serializer entry.
 */
public final class TypeRegistryImpl<T> implements TypeRegistry<T> {

    private static final Logger LOG = Logger.getLogger(TypeRegistryImpl.class.getName());

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

    @Override
    public synchronized void unregisterAll(MenuExtension owner) {
        Set<String> keys = keysByOwner.remove(owner);
        if (keys == null) return;

        for (String k : keys) {
            Class<? extends T> type = byKey.remove(k);
            if (type != null) {
                byType.remove(type);
                // NodeSerializers.unregister(Class) does not exist in hocon 1.0.6.
                // The stale serializer entry in NodeSerializers is harmless —
                // byKey is authoritative, and re-registration overwrites it.
                // serializers.unregister(type);
            }
        }
    }
}
