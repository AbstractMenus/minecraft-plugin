package ru.abstractmenus.data.properties;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import ru.abstractmenus.datatype.TypeInt;
import ru.abstractmenus.datatype.TypeMaterial;
import ru.abstractmenus.menu.item.SimpleItem;
import ru.abstractmenus.hocon.api.serialize.NodeSerializers;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.source.ConfigSources;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TestPropCount {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void buildKeepsConfiguredCountOnUnstackableItem() throws Exception {
        NodeSerializers serializers = NodeSerializers.defaults();
        serializers.register(TypeInt.class, new TypeInt.Serializer());
        serializers.register(TypeMaterial.class, new TypeMaterial.Serializer());
        var root = ConfigurationLoader.builder()
                .serializers(serializers)
                .source(ConfigSources.inputStream("test", new ByteArrayInputStream("material: DIAMOND_SWORD\ncount: 3".getBytes(StandardCharsets.UTF_8))))
                .build().load();
        SimpleItem template = new SimpleItem();
        template.addProperty("material", new PropMaterial.Serializer().deserialize(PropMaterial.class, root.node("material")));
        template.addProperty("count", new PropCount.Serializer().deserialize(PropCount.class, root.node("count")));

        ItemStack sword = template.build(null, null);

        assertNotNull(sword);
        assertEquals(Material.DIAMOND_SWORD, sword.getType());
        assertEquals(3, sword.getAmount());
    }
}
