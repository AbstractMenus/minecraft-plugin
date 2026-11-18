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
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

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
    void invalidMenusFailClosedAndLogWithOrWithoutCauseNodes() throws Exception {
        var folder = plugin.getMainConfig().getMenusFolder();
        Files.createDirectories(folder);
        var messages = new ArrayList<String>();
        var handler = new Handler() {
            @Override public void publish(LogRecord record) { messages.add(record.getMessage()); }
            @Override public void flush() {}
            @Override public void close() {}
        };
        var field = ru.abstractmenus.api.Logger.class.getDeclaredField("logger");
        field.setAccessible(true);
        var logger = (java.util.logging.Logger) field.get(null);
        logger.addHandler(handler);
        try {
            for (boolean named : new boolean[]{false, true}) {
                for (boolean unknownAction : new boolean[]{false, true}) {
                    String name = "invalid-" + named + "-" + unknownAction;
                    var file = folder.resolve(name + ".conf");
                    String body = unknownAction
                            ? "title: Shop\nsize: 1\nitems: [{slot: 0, material: DIAMOND, click {takeMony: 100}}]\n"
                            : "title: Shop\nsize: 7\n";
                    Files.writeString(file, named ? "menus {\n" + name + " {\n" + body + "}\n}\n" : body);
                    if (unknownAction) {
                        var node = ConfigurationLoader.builder().source(ConfigSources.path(file))
                                .serializers(AbstractMenusApi.get().serializers()).build().load();
                        var menuNode = named ? node.node("menus", name) : node;
                        var error = assertThrows(NodeSerializeException.class, () -> menuNode.getValue(Menu.class));
                        assertNotNull(error.getCauseNode());
                        assertEquals(named ? 5 : 3, error.getCauseNode().wrapped().origin().lineNumber());
                        assertTrue(error.getMessage().contains("takeMony"));
                    }
                }
            }
            MenuManager.instance().loadMenus();
            for (boolean named : new boolean[]{false, true}) {
                for (boolean unknownAction : new boolean[]{false, true}) {
                    String name = "invalid-" + named + "-" + unknownAction;
                    assertNull(MenuManager.instance().getMenu(name));
                    String reason = unknownAction ? "takeMony" : "Menu size";
                    assertTrue(messages.stream().anyMatch(message -> message.contains(name + ".conf")
                            && message.contains(reason) && (!unknownAction || message.contains("[Near line "))), messages.toString());
                }
            }
        } finally {
            logger.removeHandler(handler);
        }
    }

    @Test
    void pluginDisableIsClean() {
        server.getPluginManager().disablePlugin(plugin);
        assertFalse(plugin.isEnabled(), "Plugin should be disabled after disablePlugin()");
    }
}
