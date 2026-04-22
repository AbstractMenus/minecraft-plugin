package ru.abstractmenus.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.services.MenuManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * First-level MockBukkit smoke tests: the plugin boots, registers its services,
 * and shuts down cleanly. Heavier flow tests (open menu, click, actions) follow.
 *
 * Currently disabled: MockBukkit 4.108.0 + Paper 1.21.11-R0.1-SNAPSHOT fails during
 * {@code MockBukkit.mock()} with {@code PaperRegistryAccess: RegistryKeyImpl[key=minecraft:attribute]
 * points to a registry that is not available yet} (TagsMock.loadRegistry → Registry.{@literal <clinit>}).
 * Upstream compatibility issue — re-enable once MockBukkit tracks the current Paper build
 * or once we pin Paper to a version MockBukkit supports.
 */
@Disabled("MockBukkit 4.108 vs Paper 1.21.11 registry bootstrap incompatibility")
class TestPluginLifecycle {

    private ServerMock server;
    private AbstractMenus plugin;

    @BeforeEach
    void startServer() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(AbstractMenus.class);
    }

    @AfterEach
    void stopServer() {
        MockBukkit.unmock();
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
    void pluginDisableIsClean() {
        server.getPluginManager().disablePlugin(plugin);
        assertFalse(plugin.isEnabled(), "Plugin should be disabled after disablePlugin()");
    }
}
