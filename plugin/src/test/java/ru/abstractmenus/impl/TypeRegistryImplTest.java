package ru.abstractmenus.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.hocon.api.serialize.NodeSerializers;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TypeRegistryImplTest {

    interface Widget { }
    static class RedWidget implements Widget { }
    static class BlueWidget implements Widget { }

    private NodeSerializers serializers;
    private TypeRegistryImpl<Widget> registry;
    private MenuExtension ownerA;
    private MenuExtension ownerB;

    @BeforeEach
    void setUp() {
        serializers = NodeSerializers.defaults();
        registry = new TypeRegistryImpl<>(serializers);
        ownerA = new DummyExtension("A");
        ownerB = new DummyExtension("B");
    }

    @Test
    void register_exposesKey() {
        NodeSerializer<RedWidget> ser = dummySerializer();
        registry.register("red", RedWidget.class, ser, ownerA);

        assertEquals(RedWidget.class, registry.get("red"));
        assertEquals("red", registry.name(RedWidget.class));
        assertEquals(Set.of("red"), registry.keys());
    }

    @Test
    void register_isCaseInsensitive() {
        NodeSerializer<RedWidget> ser = dummySerializer();
        registry.register("Red", RedWidget.class, ser, ownerA);

        assertEquals(RedWidget.class, registry.get("RED"));
        assertEquals(RedWidget.class, registry.get("red"));
        assertEquals("red", registry.name(RedWidget.class));
    }

    @Test
    void get_returnsNullForUnknown() {
        assertNull(registry.get("nothing"));
    }

    @Test
    void unregisterAll_removesOnlyThatOwnersEntries() {
        registry.register("red",  RedWidget.class,  dummySerializer(), ownerA);
        registry.register("blue", BlueWidget.class, dummySerializer(), ownerB);

        registry.unregisterAll(ownerA);

        assertNull(registry.get("red"));
        assertEquals(BlueWidget.class, registry.get("blue"));
        assertEquals(Set.of("blue"), registry.keys());
    }

    @Test
    void register_overwrite_replaces() {
        registry.register("red", RedWidget.class,  dummySerializer(), ownerA);
        registry.register("red", BlueWidget.class, dummySerializer(), ownerB);

        assertEquals(BlueWidget.class, registry.get("red"));
        assertNull(registry.name(RedWidget.class));
    }

    @Test
    void unregisterAll_withNoRegistrations_isSafe() {
        // Should not throw even if the owner has no entries.
        registry.unregisterAll(ownerA);
        assertEquals(Set.of(), registry.keys());
    }

    @Test
    void keys_returnsLowercased() {
        registry.register("RedWidget", RedWidget.class, dummySerializer(), ownerA);
        assertEquals(Set.of("redwidget"), registry.keys());
    }

    // --- helpers ---

    /**
     * NodeSerializer<T> has one abstract method:
     *   T deserialize(Class<T>, ConfigNode) throws NodeSerializeException
     * Stubs return null (safe for tests that never actually deserialize).
     */
    @SuppressWarnings("unchecked")
    private <T> NodeSerializer<T> dummySerializer() {
        return new NodeSerializer<T>() {
            @Override
            public T deserialize(Class<T> type, ConfigNode node) throws NodeSerializeException {
                return null;
            }
        };
    }

    static class DummyExtension implements MenuExtension {
        private final String name;
        DummyExtension(String name) { this.name = name; }
        @Override public void onEnable(AbstractMenusApi api) { }
        @Override public String name() { return name; }
    }
}
