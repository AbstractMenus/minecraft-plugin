package ru.abstractmenus.datatype;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.handler.PlaceholderHandler;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.source.ConfigSources;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Shared coverage for every numeric/bool primitive Type in ru.abstractmenus.datatype.
 * Each @Nested class covers one DataType: direct-value constructor, placeholder-string
 * constructor, and the HOCON Serializer behavior (valid numeric / placeholder / throws).
 */
class TestPrimitiveDataTypes {

    private static PlaceholderHandler previousHandler;
    private static final Map<String, String> placeholderMap = new HashMap<>();

    @BeforeAll
    static void setUpPlaceholders() {
        previousHandler = Handlers.getPlaceholderHandler();
        // Minimal PlaceholderHandler that echoes lookups from the map; unresolved → returned as-is.
        Handlers.setPlaceholderHandler(new PlaceholderHandler() {
            @Override
            public String replacePlaceholder(org.bukkit.entity.Player p, String s) {
                return replace(p, s);
            }

            @Override
            public String replace(org.bukkit.entity.Player p, String s) {
                if (s == null) return null;
                String out = s;
                for (Map.Entry<String, String> e : placeholderMap.entrySet()) {
                    out = out.replace(e.getKey(), e.getValue());
                }
                return out;
            }

            @Override
            public List<String> replace(org.bukkit.entity.Player p, List<String> list) {
                return list.stream().map(s -> replace(p, s)).toList();
            }

            @Override
            public void registerAll() {}
        });
    }

    @AfterAll
    static void tearDown() {
        Handlers.setPlaceholderHandler(previousHandler);
        placeholderMap.clear();
    }

    private static ConfigNode loadValueNode(String hocon) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(hocon.getBytes(StandardCharsets.UTF_8));
        ConfigNode root = ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", in))
                .build()
                .load();
        return root.node("val");
    }

    // ----- TypeBool -----

    @Nested
    class BoolTests {
        @Test
        void directBoolReturnedAsIs() {
            assertTrue(new TypeBool(true).getBool(null, null));
            assertFalse(new TypeBool(false).getBool(null, null));
        }

        @Test
        void stringTrueResolvesToTrue() {
            assertTrue(new TypeBool("true").getBool(null, null));
            assertTrue(new TypeBool("TRUE").getBool(null, null));
        }

        @Test
        void stringNonTrueResolvesToFalse() {
            assertFalse(new TypeBool("false").getBool(null, null));
            assertFalse(new TypeBool("anything").getBool(null, null));
        }

        @Test
        void placeholderResolvedThroughHandler() {
            placeholderMap.put("%flag%", "true");
            try {
                assertTrue(new TypeBool("%flag%").getBool(null, null));
            } finally {
                placeholderMap.clear();
            }
        }

        @Test
        void serializerAcceptsBoolNode() throws Exception {
            TypeBool result = new TypeBool.Serializer()
                    .deserialize(TypeBool.class, loadValueNode("val = true"));
            assertTrue(result.getBool(null, null));
        }

        @Test
        void serializerAcceptsStringNode() throws Exception {
            TypeBool result = new TypeBool.Serializer()
                    .deserialize(TypeBool.class, loadValueNode("val = \"%some_var%\""));
            assertNotNull(result);
        }
    }

    // ----- TypeInt -----

    @Nested
    class IntTests {
        @Test
        void directIntReturnedAsIs() {
            assertEquals(42, new TypeInt(42).getInt(null, null));
            assertEquals(-7, new TypeInt(-7).getInt(null, null));
        }

        @Test
        void serializerAcceptsIntegerNode() throws Exception {
            TypeInt result = new TypeInt.Serializer()
                    .deserialize(TypeInt.class, loadValueNode("val = 15"));
            assertEquals(15, result.getInt(null, null));
        }

        @Test
        void serializerAcceptsPlaceholderString() throws Exception {
            TypeInt result = new TypeInt.Serializer()
                    .deserialize(TypeInt.class, loadValueNode("val = \"%count%\""));
            assertNotNull(result);
        }

        @Test
        void serializerRejectsPlainStringWithoutPlaceholder() {
            assertThrows(NodeSerializeException.class, () -> new TypeInt.Serializer()
                    .deserialize(TypeInt.class, loadValueNode("val = \"not-a-number\"")));
        }

        @Test
        void placeholderNumericValueIsTruncatedFromDouble() {
            placeholderMap.put("%x%", "3.9");
            try {
                // Source: Double.valueOf(str).intValue() — truncates toward zero.
                assertEquals(3, new TypeInt("%x%").getInt(null, null));
            } finally {
                placeholderMap.clear();
            }
        }
    }

    // ----- TypeByte -----

    @Nested
    class ByteTests {
        @Test
        void directByteReturnedAsIs() {
            assertEquals((byte) 10, new TypeByte((byte) 10).getByte(null, null));
        }

        @Test
        void serializerAcceptsIntegerAndNarrows() throws Exception {
            TypeByte result = new TypeByte.Serializer()
                    .deserialize(TypeByte.class, loadValueNode("val = 127"));
            assertEquals((byte) 127, result.getByte(null, null));
        }

        @Test
        void serializerRejectsPlainString() {
            assertThrows(NodeSerializeException.class, () -> new TypeByte.Serializer()
                    .deserialize(TypeByte.class, loadValueNode("val = \"abc\"")));
        }

        @Test
        void byteParsingThrowsOnOutOfRangePlaceholder() {
            placeholderMap.put("%b%", "300");
            try {
                assertThrows(NumberFormatException.class,
                        () -> new TypeByte("%b%").getByte(null, null));
            } finally {
                placeholderMap.clear();
            }
        }
    }

    // ----- TypeShort -----

    @Nested
    class ShortTests {
        @Test
        void directShortReturnedAsIs() {
            assertEquals((short) 1234, new TypeShort((short) 1234).getShort(null, null));
        }

        @Test
        void serializerAcceptsInteger() throws Exception {
            TypeShort result = new TypeShort.Serializer()
                    .deserialize(TypeShort.class, loadValueNode("val = 32000"));
            assertEquals((short) 32000, result.getShort(null, null));
        }

        @Test
        void serializerRejectsNonPlaceholderString() {
            assertThrows(NodeSerializeException.class, () -> new TypeShort.Serializer()
                    .deserialize(TypeShort.class, loadValueNode("val = \"plain\"")));
        }
    }

    // ----- TypeLong -----

    @Nested
    class LongTests {
        @Test
        void directLongReturnedAsIs() {
            assertEquals(123L, new TypeLong(123).getLong(null, null));
        }

        @Test
        void serializerAcceptsIntegerNode() throws Exception {
            TypeLong result = new TypeLong.Serializer()
                    .deserialize(TypeLong.class, loadValueNode("val = 100"));
            assertEquals(100L, result.getLong(null, null));
        }

        @Test
        void serializerRejectsPlainString() {
            assertThrows(NodeSerializeException.class, () -> new TypeLong.Serializer()
                    .deserialize(TypeLong.class, loadValueNode("val = \"abc\"")));
        }
    }

    // ----- TypeFloat -----

    @Nested
    class FloatTests {
        @Test
        void directFloatReturnedAsIs() {
            assertEquals(1.5f, new TypeFloat(1.5f).getFloat(null, null), 0f);
        }

        @Test
        void serializerAcceptsNumericNode() throws Exception {
            TypeFloat result = new TypeFloat.Serializer()
                    .deserialize(TypeFloat.class, loadValueNode("val = 2.75"));
            assertEquals(2.75f, result.getFloat(null, null), 0f);
        }

        @Test
        void serializerAcceptsIntegerNode() throws Exception {
            TypeFloat result = new TypeFloat.Serializer()
                    .deserialize(TypeFloat.class, loadValueNode("val = 3"));
            assertEquals(3f, result.getFloat(null, null), 0f);
        }

        @Test
        void serializerRejectsPlainString() {
            assertThrows(NodeSerializeException.class, () -> new TypeFloat.Serializer()
                    .deserialize(TypeFloat.class, loadValueNode("val = \"abc\"")));
        }
    }

    // ----- TypeDouble -----

    @Nested
    class DoubleTests {
        @Test
        void directDoubleReturnedAsIs() {
            assertEquals(3.14, new TypeDouble(3.14).getDouble(null, null), 0);
        }

        @Test
        void serializerAcceptsNumericNode() throws Exception {
            TypeDouble result = new TypeDouble.Serializer()
                    .deserialize(TypeDouble.class, loadValueNode("val = 9.81"));
            assertEquals(9.81, result.getDouble(null, null), 0);
        }

        @Test
        void serializerAcceptsIntegerNode() throws Exception {
            TypeDouble result = new TypeDouble.Serializer()
                    .deserialize(TypeDouble.class, loadValueNode("val = 4"));
            assertEquals(4.0, result.getDouble(null, null), 0);
        }

        @Test
        void serializerRejectsPlainString() {
            assertThrows(NodeSerializeException.class, () -> new TypeDouble.Serializer()
                    .deserialize(TypeDouble.class, loadValueNode("val = \"xyz\"")));
        }

        @Test
        void placeholderParsedAsDouble() {
            placeholderMap.put("%d%", "1.25");
            try {
                assertEquals(1.25, new TypeDouble("%d%").getDouble(null, null), 0);
            } finally {
                placeholderMap.clear();
            }
        }
    }

    // ----- DataType.getValue / common contract -----

    @Nested
    class CommonContractTests {
        @Test
        void valueAccessorReturnsOriginalString() {
            assertEquals("%hp%", new TypeInt("%hp%").getValue());
        }

        @Test
        void valueAccessorNullWhenDirectConstructor() {
            assertNull(new TypeInt(5).getValue());
        }
    }
}
