package ru.abstractmenus.data.properties;

import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.abstractmenus.datatype.TypeBool;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.source.ConfigSources;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@code tooltipDisplay} item property's config parsing and its
 * {@code hiddenComponents} component resolver.
 *
 * <p>The resolver is backed by the injectable {@link Registry#DATA_COMPONENT_TYPE}
 * source, so case-insensitive / namespaced resolution and unknown-component
 * handling are covered without a live server (see
 * {@code PropTooltipDisplay.configureRegistry}).
 *
 * <p>Full {@code apply()} verification needs the Paper {@code TooltipDisplay}
 * builder bridge, which requires a booted server (MockBukkit cannot bootstrap
 * Paper 1.21.11) — see README's Tests section.
 */
class TestPropTooltipDisplay {

    private static final Map<String, DataComponentType> COMPONENTS = new LinkedHashMap<>();

    @BeforeEach
    void setUp() {
        COMPONENTS.clear();
        COMPONENTS.put("bundle_contents", component("bundle_contents"));
        COMPONENTS.put("enchantments", component("enchantments"));
        COMPONENTS.put("attribute_modifiers", component("attribute_modifiers"));
        COMPONENTS.put("stored_enchantments", component("stored_enchantments"));
        COMPONENTS.put("custom_model_data", component("custom_model_data"));

        PropTooltipDisplay.configureRegistry(COMPONENTS.values());
    }

    @AfterEach
    void tearDown() {
        PropTooltipDisplay.resetRegistry();
    }

    // ----- resolver -----

    @Test
    void resolvesUppercaseName() {
        assertSame(COMPONENTS.get("bundle_contents"),
                PropTooltipDisplay.resolveComponent("BUNDLE_CONTENTS"));
    }

    @Test
    void resolvesLowercaseName() {
        assertSame(COMPONENTS.get("bundle_contents"),
                PropTooltipDisplay.resolveComponent("bundle_contents"));
    }

    @Test
    void resolvesNamespacedKey() {
        assertSame(COMPONENTS.get("bundle_contents"),
                PropTooltipDisplay.resolveComponent("minecraft:bundle_contents"));
    }

    @Test
    void resolveIsCaseInsensitiveForNamespacedInput() {
        assertSame(COMPONENTS.get("bundle_contents"),
                PropTooltipDisplay.resolveComponent("MINECRAFT:BUNDLE_CONTENTS"));
    }

    @Test
    void unknownComponentResolvesToNull() {
        assertNull(PropTooltipDisplay.resolveComponent("NOPE_NOT_A_COMPONENT"));
    }

    @Test
    void nullComponentResolvesToNull() {
        assertNull(PropTooltipDisplay.resolveComponent(null));
        assertNull(PropTooltipDisplay.resolveComponent(" "));
    }

    // ----- serialization -----

    @Test
    void parsesHideTooltipTrue() throws Exception {
        PropTooltipDisplay prop = build("tooltipDisplay { hideTooltip: true }");
        TypeBool hide = (TypeBool) field(prop, "hideTooltip");
        assertNotNull(hide);
        assertTrue(hide.getBool(null, null));
    }

    @Test
    void parsesHideTooltipFalse() throws Exception {
        PropTooltipDisplay prop = build("tooltipDisplay { hideTooltip: false }");
        TypeBool hide = (TypeBool) field(prop, "hideTooltip");
        assertNotNull(hide);
        assertFalse(hide.getBool(null, null));
    }

    @Test
    void parsesHiddenComponentsCaseInsensitive() throws Exception {
        PropTooltipDisplay prop = build("tooltipDisplay { hiddenComponents: [BUNDLE_CONTENTS] }");
        Set<DataComponentType> hidden = hidden(prop);
        assertEquals(Set.of(COMPONENTS.get("bundle_contents")), hidden);
    }

    @Test
    void parsesHiddenComponentsNamespaced() throws Exception {
        PropTooltipDisplay prop = build("tooltipDisplay { hiddenComponents: [\"minecraft:bundle_contents\"] }");
        assertEquals(Set.of(COMPONENTS.get("bundle_contents")), hidden(prop));
    }

    @Test
    void parsesMultipleHiddenComponents() throws Exception {
        PropTooltipDisplay prop = build(
                "tooltipDisplay { hiddenComponents: [BUNDLE_CONTENTS, ENCHANTMENTS, attribute_modifiers] }");
        assertEquals(Set.of(
                COMPONENTS.get("bundle_contents"),
                COMPONENTS.get("enchantments"),
                COMPONENTS.get("attribute_modifiers")), hidden(prop));
    }

    @Test
    void unknownComponentThrows() throws Exception {
        ConfigNode root = loadNode("tooltipDisplay { hiddenComponents: [FUCKING_UNKNOWN_COMPONENT] }");
        PropTooltipDisplay.Serializer serializer = new PropTooltipDisplay.Serializer();
        assertThrows(NodeSerializeException.class, () ->
                serializer.deserialize(PropTooltipDisplay.class, root.node("tooltipDisplay")));
    }

    @Test
    void emptyHiddenComponentsIsAccepted() throws Exception {
        PropTooltipDisplay prop = build("tooltipDisplay { hiddenComponents: [] }");
        assertEquals(Set.of(), hidden(prop));
    }

    @Test
    void emptyTooltipDisplayLeavesFieldsNull() throws Exception {
        PropTooltipDisplay prop = build("tooltipDisplay { }");
        assertNull(field(prop, "hideTooltip"));
        assertNull(field(prop, "hiddenComponents"));
    }

    @Test
    void hideTooltipOnlyLeavesHiddenNull() throws Exception {
        PropTooltipDisplay prop = build("tooltipDisplay { hideTooltip: false }");
        assertNull(field(prop, "hiddenComponents"));
    }

    // ----- helpers -----

    private static PropTooltipDisplay build(String hocon) throws Exception {
        ConfigNode root = loadNode(hocon);
        return new PropTooltipDisplay.Serializer().deserialize(PropTooltipDisplay.class, root.node("tooltipDisplay"));
    }

    @SuppressWarnings("unchecked")
    private static Set<DataComponentType> hidden(PropTooltipDisplay prop) throws Exception {
        return (Set<DataComponentType>) field(prop, "hiddenComponents");
    }

    private static Object field(Object target, String name) throws Exception {
        Field f = PropTooltipDisplay.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    private static ConfigNode loadNode(String hocon) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(hocon.getBytes(StandardCharsets.UTF_8));
        return ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", in))
                .build()
                .load();
    }

    private static DataComponentType component(String key) {
        DataComponentType type = mock(DataComponentType.class);
        when(type.getKey()).thenReturn(NamespacedKey.minecraft(key));
        return type;
    }
}
