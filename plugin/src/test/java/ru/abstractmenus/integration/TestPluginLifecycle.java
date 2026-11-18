package ru.abstractmenus.integration;

import io.papermc.paper.configuration.ServerConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.source.ConfigSources;
import ru.abstractmenus.services.MenuManager;
import ru.abstractmenus.util.bukkit.BukkitTasks;
import ru.abstractmenus.util.bukkit.Events;

import java.nio.file.Files;

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
    void removedJsRuleRejectsMenuAtItsSourceLine() throws Exception {
        var file = plugin.getMainConfig().getMenusFolder().resolve("unsafe-js.conf");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "title: Unsafe\nsize: 1\nrules {\n  js: true\n}\n");
        var node = ConfigurationLoader.builder().source(ConfigSources.path(file))
                .serializers(AbstractMenusApi.get().serializers()).build().load();
        var error = assertThrows(NodeSerializeException.class, () -> node.getValue(Menu.class));
        assertNotNull(error.getCauseNode());
        assertEquals(4, error.getCauseNode().wrapped().origin().lineNumber());
        assertTrue(error.getMessage().contains("removed because it allowed code injection"));
        assertTrue(error.getMessage().contains("'if'"));
        MenuManager.instance().loadMenus();
        assertNull(MenuManager.instance().getMenu("unsafe-js"));
    }

    @Test
    void pluginDisableIsClean() {
        server.getPluginManager().disablePlugin(plugin);
        assertFalse(plugin.isEnabled(), "Plugin should be disabled after disablePlugin()");
    }
}
