package ru.abstractmenus.integration;

import io.papermc.paper.configuration.ServerConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.services.MenuManager;
import ru.abstractmenus.util.bukkit.BukkitTasks;
import ru.abstractmenus.util.bukkit.Events;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestPluginLifecycle {

    private ServerMock server;
    private AbstractMenus plugin;

    @BeforeEach
    void startServer() {
        server = MockBukkit.mock(new ServerMock() {
            private final ServerConfiguration configuration = mock(ServerConfiguration.class);

            @Override
            public ServerConfiguration getServerConfig() {
                return configuration;
            }
        });
        plugin = MockBukkit.load(AbstractMenus.class);
    }

    @AfterEach
    void stopServer() {
        try {
            MockBukkit.unmock();
        } finally {
            BukkitTasks.setFoliaLib(null);
            BukkitTasks.setPlugin(null);
            Events.setPlugin(null);
        }
    }

    @Test
    void pluginEnabledAfterLoad() {
        assertNotNull(plugin);
        assertTrue(plugin.isEnabled(), "Plugin should be enabled after load");
    }

    @Test
    void menuManagerSingletonIsInitialized() {
        assertNotNull(MenuManager.instance(),
                "MenuManager.instance() must be set during onEnable()");
    }

    @Test
    void bundledCakeUsesVanillaTooltipStyle() throws Exception {
        var manager = MenuManager.instance();
        manager.loadMenus();
        var player = server.addPlayer();
        var template = manager.getMenu("menu");
        assertNotNull(template);
        manager.openMenu(player, template);
        var cake = manager.getOpenedMenu(player).getInventory().getItem(4);
        assertNotNull(cake);
        assertEquals(org.bukkit.Material.CAKE, cake.getType());
        assertFalse(cake.getItemMeta().hasTooltipStyle());
    }

    @Test
    void pluginDisableIsClean() {
        server.getPluginManager().disablePlugin(plugin);
        assertFalse(plugin.isEnabled(), "Plugin should be disabled after disablePlugin()");
    }
}
