package ru.abstractmenus.extractors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestItemStackExtractor {
    @BeforeEach
    void setUp() { MockBukkit.mock(); }

    @AfterEach
    void tearDown() { MockBukkit.unmock(); }

    @Test
    void localizedNameUsesDisplayName() {
        ItemStack item = new ItemStack(Material.STONE);
        assertEquals("", ItemStackExtractor.INSTANCE.extract(item, "item_localized_name"));
        var meta = item.getItemMeta();
        meta.setDisplayName("Menu stone");
        item.setItemMeta(meta);
        assertEquals("Menu stone", ItemStackExtractor.INSTANCE.extract(item, "item_localized_name"));
    }
}
