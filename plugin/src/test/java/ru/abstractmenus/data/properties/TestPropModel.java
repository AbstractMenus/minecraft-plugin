package ru.abstractmenus.data.properties;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.source.ConfigSources;
import ru.abstractmenus.menu.item.SimpleItem;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestPropModel {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void legacyIntegerBecomesSingleFloat() throws Exception {
        CustomModelDataComponent model = build("model: 123");
        assertEquals(List.of(123.0f), model.getFloats());
        assertTrue(model.getFlags().isEmpty());
        assertTrue(model.getStrings().isEmpty());
        assertTrue(model.getColors().isEmpty());
    }

    @Test
    void objectRetainsAllFourTypedLists() throws Exception {
        CustomModelDataComponent model = build("""
                model {
                    floats: [1, 2.5]
                    flags: [true, false]
                    strings: ["cake", "bundle"]
                    colors: ["#ff8000", "0, 64, 255"]
                }
                """);
        assertEquals(List.of(1.0f, 2.5f), model.getFloats());
        assertEquals(List.of(true, false), model.getFlags());
        assertEquals(List.of("cake", "bundle"), model.getStrings());
        assertEquals(List.of(Color.fromRGB(255, 128, 0), Color.fromRGB(0, 64, 255)), model.getColors());
    }

    private static CustomModelDataComponent build(String hocon) throws Exception {
        ConfigNode root = ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", new ByteArrayInputStream(hocon.getBytes(StandardCharsets.UTF_8))))
                .build().load();
        SimpleItem template = new SimpleItem();
        template.addProperty("model", new PropModel.Serializer().deserialize(PropModel.class, root.node("model")));
        ItemStack item = template.build(null, null);
        assertNotNull(item);
        return item.getItemMeta().getCustomModelDataComponent();
    }
}
