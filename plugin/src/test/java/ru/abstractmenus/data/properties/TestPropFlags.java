package ru.abstractmenus.data.properties;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.source.ConfigSources;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TestPropFlags {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void hidingAttributesPreservesCustomModifiers() throws Exception {
        var root = ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", new ByteArrayInputStream("flags: HIDE_ATTRIBUTES".getBytes(StandardCharsets.UTF_8))))
                .build().load();
        PropFlags property = new PropFlags.Serializer().deserialize(PropFlags.class, root.node("flags"));
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        AttributeModifier modifier = new AttributeModifier(NamespacedKey.minecraft("test_damage"), 12, AttributeModifier.Operation.ADD_NUMBER);
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, modifier);

        property.apply(sword, meta, null, null);
        sword.setItemMeta(meta);

        assertTrue(sword.getItemMeta().hasItemFlag(ItemFlag.HIDE_ATTRIBUTES));
        assertEquals(java.util.List.of(modifier), java.util.List.copyOf(sword.getItemMeta().getAttributeModifiers(Attribute.ATTACK_DAMAGE)));
    }
}
