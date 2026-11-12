package ru.abstractmenus.data.properties;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.source.ConfigSources;
import ru.abstractmenus.menu.item.SimpleItem;
import ru.abstractmenus.util.MiniMessageUtil;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TestPropTooltipDisplay {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        MiniMessageUtil.init(false);
        Colors.init(false);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void survivesMetaPropertiesOnBothSidesWhenBuildingMenuItem() throws Exception {
        ConfigNode root = loadNode("""
                name: "Named bundle"
                tooltipDisplay {
                    hideTooltip: true
                    hiddenComponents: [BUNDLE_CONTENTS, "minecraft:enchantments"]
                }
                lore: ["Still named and hidden"]
                """);
        SimpleItem template = new SimpleItem();
        template.addProperty("name", new PropName.Serializer().deserialize(PropName.class, root.node("name")));
        PropTooltipDisplay tooltipProperty = spy(new PropTooltipDisplay.Serializer().deserialize(PropTooltipDisplay.class, root.node("tooltipDisplay")));
        // MockBukkit retains component data when stale meta is applied, so also verify the flush boundary.
        doAnswer(invocation -> {
            ItemStack stack = invocation.getArgument(0);
            assertEquals("Named bundle", stack.getItemMeta().getDisplayName());
            return invocation.callRealMethod();
        }).when(tooltipProperty).apply(any(), any(), any(), any());
        template.addProperty("tooltipDisplay", tooltipProperty);
        template.addProperty("lore", new PropLore.Serializer().deserialize(PropLore.class, root.node("lore")));

        ItemStack item = template.build(null, null);

        assertNotNull(item);
        assertEquals("Named bundle", item.getItemMeta().getDisplayName());
        assertEquals(List.of("Still named and hidden"), item.getItemMeta().getLore());
        TooltipDisplay tooltip = item.getData(DataComponentTypes.TOOLTIP_DISPLAY);
        assertNotNull(tooltip);
        assertTrue(tooltip.hideTooltip());
        assertEquals(Set.of(DataComponentTypes.BUNDLE_CONTENTS, DataComponentTypes.ENCHANTMENTS), tooltip.hiddenComponents());
    }

    @Test
    void rejectsUnknownComponentAndNonList() throws Exception {
        ConfigNode root = loadNode("""
                unknown { hiddenComponents: [UNKNOWN_COMPONENT] }
                invalid { hiddenComponents: BUNDLE_CONTENTS }
                """);
        PropTooltipDisplay.Serializer serializer = new PropTooltipDisplay.Serializer();
        assertThrows(NodeSerializeException.class, () -> serializer.deserialize(PropTooltipDisplay.class, root.node("unknown")));
        assertThrows(NodeSerializeException.class, () -> serializer.deserialize(PropTooltipDisplay.class, root.node("invalid")));
    }

    private static ConfigNode loadNode(String hocon) throws Exception {
        return ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", new ByteArrayInputStream(hocon.getBytes(StandardCharsets.UTF_8))))
                .build().load();
    }
}
