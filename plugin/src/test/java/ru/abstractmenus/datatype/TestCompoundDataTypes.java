package ru.abstractmenus.datatype;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.handler.PlaceholderHandler;
import ru.abstractmenus.api.inventory.Slot;
import ru.abstractmenus.api.inventory.slot.SlotIndex;
import ru.abstractmenus.api.inventory.slot.SlotPos;
import ru.abstractmenus.api.inventory.slot.SlotRange;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.source.ConfigSources;
import ru.abstractmenus.util.SlotUtil;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers compound DataType implementations:
 * - TypeSlot: single index / "x,y" SlotPos / "from-to" SlotRange / matrix list
 * - TypeEnum: generic enum wrapper
 * - TypeColor: hex, RGB triple, DyeColor constant
 */
class TestCompoundDataTypes {

    private static PlaceholderHandler previousHandler;

    @BeforeAll
    static void installIdentityPlaceholderHandler() {
        previousHandler = Handlers.getPlaceholderHandler();
        // Identity handler — tests in this file don't exercise actual replacement.
        Handlers.setPlaceholderHandler(new PlaceholderHandler() {
            @Override public String replacePlaceholder(org.bukkit.entity.Player p, String s) { return s; }
            @Override public String replace(org.bukkit.entity.Player p, String s) { return s; }
            @Override public List<String> replace(org.bukkit.entity.Player p, List<String> l) { return l; }
            @Override public void registerAll() {}
        });
    }

    @AfterAll
    static void restorePlaceholderHandler() {
        Handlers.setPlaceholderHandler(previousHandler);
    }

    private static ConfigNode loadValueNode(String hocon) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(hocon.getBytes(StandardCharsets.UTF_8));
        ConfigNode root = ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", in))
                .build()
                .load();
        return root.node("val");
    }

    // ----- TypeSlot -----

    @Nested
    class SlotSerializerTests {

        @Test
        void integerNodeBecomesSlotIndex() throws Exception {
            TypeSlot result = new TypeSlot.Serializer()
                    .deserialize(TypeSlot.class, loadValueNode("val = 5"));
            Slot slot = result.getSlot(null, null);
            assertInstanceOf(SlotIndex.class, slot);
            assertEquals(5, ((SlotIndex) slot).getIndex());
        }

        @Test
        void commaStringBecomesSlotPos() throws Exception {
            TypeSlot result = new TypeSlot.Serializer()
                    .deserialize(TypeSlot.class, loadValueNode("val = \"2,3\""));
            Slot slot = result.getSlot(null, null);
            assertInstanceOf(SlotPos.class, slot);
            // SlotPos is 1-indexed: (y-1)*9 + (x-1) → (3-1)*9 + (2-1) = 19
            Collection<Integer> indices = SlotUtil.collect(slot);
            assertEquals(1, indices.size());
            assertTrue(indices.contains(19));
        }

        @Test
        void dashStringBecomesSlotRange() throws Exception {
            TypeSlot result = new TypeSlot.Serializer()
                    .deserialize(TypeSlot.class, loadValueNode("val = \"3-5\""));
            Slot slot = result.getSlot(null, null);
            assertInstanceOf(SlotRange.class, slot);
            Collection<Integer> indices = SlotUtil.collect(slot);
            assertEquals(3, indices.size());
            assertTrue(indices.containsAll(List.of(3, 4, 5)));
        }

        @Test
        void placeholderStringPreservedAsLazy() throws Exception {
            // A string with a placeholder defers parsing to runtime (requires PlaceholderHandler).
            TypeSlot result = new TypeSlot.Serializer()
                    .deserialize(TypeSlot.class, loadValueNode("val = \"%dynamic_slot%\""));
            assertNotNull(result);
            assertEquals("%dynamic_slot%", result.getValue());
        }

        @Test
        void listNodeBecomesMatrixWithExpectedSlots() throws Exception {
            // 3 rows of 9 chars each; '-' skips, anything else fills.
            // Row 0: "x--------" → slot 0
            // Row 1: "-x-------" → slot 10
            // Row 2: "--x------" → slot 20
            String hocon = "val = [\"x--------\", \"-x-------\", \"--x------\"]";
            TypeSlot result = new TypeSlot.Serializer()
                    .deserialize(TypeSlot.class, loadValueNode(hocon));
            Collection<Integer> indices = SlotUtil.collect(result.getSlot(null, null));
            assertEquals(3, indices.size());
            assertTrue(indices.containsAll(List.of(0, 10, 20)));
        }

        @Test
        void plainIntegerStringBecomesSlotIndex() {
            Slot parsed = TypeSlot.Serializer.parseStr("7");
            assertInstanceOf(SlotIndex.class, parsed);
            assertEquals(7, ((SlotIndex) parsed).getIndex());
        }

        @Test
        void malformedStringThrows() {
            assertThrows(RuntimeException.class,
                    () -> TypeSlot.Serializer.parseStr("not-a-slot"));
        }

        @Test
        void directSlotConstructorReturnedAsIs() {
            SlotIndex backing = new SlotIndex(4);
            TypeSlot wrapper = new TypeSlot(backing);
            assertSame(backing, wrapper.getSlot(null, null));
        }
    }

    // ----- TypeEnum -----

    enum Mood { HAPPY, SAD, ANGRY }

    @Nested
    class EnumTests {

        @Test
        void directValueReturnedAsIs() {
            TypeEnum<Mood> wrapper = new TypeEnum<>(Mood.HAPPY);
            assertEquals(Mood.HAPPY, wrapper.getEnum(Mood.class, null, null));
        }

        @Test
        void stringValueResolvedViaValueOf() {
            TypeEnum<Mood> wrapper = new TypeEnum<>("SAD");
            assertEquals(Mood.SAD, wrapper.getEnum(Mood.class, null, null));
        }

        @Test
        void lowercaseIsNormalizedToUpperBeforeValueOf() {
            TypeEnum<Mood> wrapper = new TypeEnum<>("angry");
            assertEquals(Mood.ANGRY, wrapper.getEnum(Mood.class, null, null));
        }

        @Test
        void unknownValueThrows() {
            TypeEnum<Mood> wrapper = new TypeEnum<>("PUZZLED");
            assertThrows(IllegalArgumentException.class,
                    () -> wrapper.getEnum(Mood.class, null, null));
        }

        @Test
        void serializerAlwaysProducesStringBacked() throws Exception {
            // Note: Serializer stashes the raw node string; resolution happens at getEnum().
            TypeEnum result = new TypeEnum.Serializer()
                    .deserialize(TypeEnum.class, loadValueNode("val = HAPPY"));
            assertEquals("HAPPY", result.getValue());
        }
    }

    // ----- TypeColor -----

    @Nested
    class ColorTests {

        @Test
        void hexStringBecomesColor() throws Exception {
            TypeColor result = new TypeColor.Serializer()
                    .deserialize(TypeColor.class, loadValueNode("val = \"#FF8800\""));
            Color color = result.getColor(null, null);
            assertEquals(0xFF, color.getRed());
            assertEquals(0x88, color.getGreen());
            assertEquals(0x00, color.getBlue());
        }

        @Test
        void commaSeparatedRgbBecomesColor() throws Exception {
            TypeColor result = new TypeColor.Serializer()
                    .deserialize(TypeColor.class, loadValueNode("val = \"10, 20, 30\""));
            Color color = result.getColor(null, null);
            assertEquals(10, color.getRed());
            assertEquals(20, color.getGreen());
            assertEquals(30, color.getBlue());
        }

        @Test
        void dyeColorConstantResolvesToMatchingColor() throws Exception {
            TypeColor result = new TypeColor.Serializer()
                    .deserialize(TypeColor.class, loadValueNode("val = RED"));
            assertEquals(DyeColor.RED.getColor(), result.getColor(null, null));
        }

        @Test
        void dyeColorConstantCaseInsensitive() throws Exception {
            TypeColor result = new TypeColor.Serializer()
                    .deserialize(TypeColor.class, loadValueNode("val = blue"));
            assertEquals(DyeColor.BLUE.getColor(), result.getColor(null, null));
        }

        @Test
        void unknownConstantThrows() {
            assertThrows(NodeSerializeException.class, () -> new TypeColor.Serializer()
                    .deserialize(TypeColor.class, loadValueNode("val = \"NOT_A_COLOR\"")));
        }

        @Test
        void placeholderStringDoesNotParseEagerly() throws Exception {
            // Placeholder strings must survive deserialization without crashing.
            TypeColor result = new TypeColor.Serializer()
                    .deserialize(TypeColor.class, loadValueNode("val = \"%color%\""));
            assertEquals("%color%", result.getValue());
        }

        @Test
        void directColorConstructorReturnedAsIs() {
            Color green = Color.GREEN;
            assertEquals(green, new TypeColor(green).getColor(null, null));
        }
    }
}
