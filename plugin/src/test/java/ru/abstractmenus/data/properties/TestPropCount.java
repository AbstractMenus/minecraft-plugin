package ru.abstractmenus.data.properties;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import ru.abstractmenus.datatype.TypeInt;
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
    void countAboveNaturalLimitIsClampedForUnstackableItem() throws Exception {
        NodeSerializers serializers = NodeSerializers.defaults();
        serializers.register(TypeInt.class, new TypeInt.Serializer());
        var root = ConfigurationLoader.builder()
                .serializers(serializers)
                .source(ConfigSources.inputStream("test", new ByteArrayInputStream("count: 64".getBytes(StandardCharsets.UTF_8))))
                .build().load();
        PropCount property = new PropCount.Serializer().deserialize(PropCount.class, root.node("count"));
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemStack stone = new ItemStack(Material.STONE);

        property.apply(sword, sword.getItemMeta(), null, null);
        property.apply(stone, stone.getItemMeta(), null, null);

        assertEquals(1, sword.getAmount());
        assertEquals(64, stone.getAmount());
    }
}
