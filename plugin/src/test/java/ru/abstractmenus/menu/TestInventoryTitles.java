package ru.abstractmenus.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.ServicePriority;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.inventory.InventoryMock;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.util.MiniMessageUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TestInventoryTitles {

    private Player player;

    @BeforeEach
    void startServer() {
        var server = MockBukkit.mock();
        var api = mock(AbstractMenusApi.class, RETURNS_DEEP_STUBS);
        when(api.providers().placeholders().resolve().replace(any(), anyString()))
                .thenAnswer(call -> call.<String>getArgument(1).replace("%title%", "Menu"));
        server.getServicesManager().register(AbstractMenusApi.class, api, MockBukkit.createMockPlugin(), ServicePriority.Normal);
        player = server.addPlayer();
        Colors.init(false);
    }

    @AfterEach
    void stopServer() {
        MiniMessageUtil.init(false);
        MockBukkit.unmock();
    }

    @Test
    void legacyTitleProducesComponentWithAndWithoutMiniMessage() {
        for (boolean miniMessage : new boolean[]{false, true}) {
            MiniMessageUtil.init(miniMessage);
            assertEquals(Component.text("Menu", NamedTextColor.GREEN, TextDecoration.BOLD), titleOf("&a&l%title%", null));
        }
    }

    @Test
    void miniMessageTitleRetainsComponentEventsAndFormattingForTypedInventory() {
        MiniMessageUtil.init(true);
        Component expected = Component.text("Menu", NamedTextColor.GOLD, TextDecoration.BOLD)
                .hoverEvent(Component.text("Details"));
        assertEquals(expected, titleOf("<gold><bold><hover:show_text:'Details'>%title%</hover></bold></gold>", InventoryType.HOPPER));
    }

    @Test
    void hexTitlesPreserveRgbColors() {
        Component expected = Component.text("Menu", TextColor.color(0x12abef));
        for (boolean miniMessage : new boolean[]{false, true}) {
            MiniMessageUtil.init(miniMessage);
            assertEquals(expected, titleOf("&x&1&2&a&b&e&f%title%", null));
        }
        MiniMessageUtil.init(true);
        assertEquals(expected, titleOf("<#12abef>%title%", null));
    }

    private Component titleOf(String title, InventoryType type) {
        var menu = new SimpleMenu(title, 9);
        menu.setType(type);
        menu.createInventory(player, menu);
        return ((InventoryMock) menu.getInventory()).getCustomTitle();
    }
}
