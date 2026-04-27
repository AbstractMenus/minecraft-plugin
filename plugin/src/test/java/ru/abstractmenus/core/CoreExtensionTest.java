package ru.abstractmenus.core;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.Catalog;
import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.ProviderRegistry;
import ru.abstractmenus.impl.ProviderRegistryImpl;
import ru.abstractmenus.api.TypeRegistry;
import ru.abstractmenus.impl.TypeRegistryImpl;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.variables.VariableManager;
import ru.abstractmenus.hocon.api.serialize.NodeSerializers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Verifies that {@link CoreExtension#onEnable(AbstractMenusApi)} registers the
 * expected number of core types into the five registries.
 *
 * <p>This test does NOT boot the full plugin — it constructs a lightweight
 * {@link StubApi} backed by real {@link TypeRegistryImpl} instances and invokes
 * {@code CoreExtension.onEnable()} directly, bypassing the MockBukkit
 * incompatibility with Paper 1.21.11 that prevents the full plugin from loading
 * in tests (see {@link ru.abstractmenus.integration.TestPluginLifecycle}).
 *
 * <p><b>Activator count note:</b> {@link CoreActivatorsBundle} conditionally
 * registers "clickNPC" (Citizens) and "regionJoin"/"regionLeave" (WorldGuard)
 * via {@code AbstractMenus.checkDependency()}. In this test environment those
 * soft-deps are absent, so the activator count is 14 (not 17 as in full
 * production). The 17 figure in the Phase B.1 spec counts all three conditional
 * registrations as present.
 */
class CoreExtensionTest {

    private StubApi api;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    void setUp() {
        // CoreActivatorsBundle calls AbstractMenus.checkDependency() which calls
        // Bukkit.getServer().getPluginManager().isPluginEnabled(...).
        // Mock the static Bukkit.getServer() so the call doesn't NPE.
        // Return false for every plugin so only unconditional activators register.
        PluginManager pluginManager = mock(PluginManager.class);
        when(pluginManager.isPluginEnabled(anyString())).thenReturn(false);

        Server server = mock(Server.class);
        when(server.getPluginManager()).thenReturn(pluginManager);

        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getServer).thenReturn(server);

        api = new StubApi();
    }

    @AfterEach
    void tearDown() {
        mockedBukkit.close();
    }

    @Test
    void coreExtension_registersExpectedActions() {
        new CoreExtension().onEnable(api);
        assertEquals(65, api.actions().keys().size(),
                "expected 65 core action types (matches Task 8 migration count)");
    }

    @Test
    void coreExtension_registersExpectedRules() {
        new CoreExtension().onEnable(api);
        assertEquals(28, api.rules().keys().size(),
                "expected 28 core rule types (matches Task 9 migration count)");
    }

    @Test
    void coreExtension_registersExpectedItemProperties() {
        new CoreExtension().onEnable(api);
        assertEquals(32, api.itemProperties().keys().size(),
                "expected 32 core item-property types (matches Task 10 migration count)");
    }

    @Test
    void coreExtension_registersExpectedActivators_withoutOptionalDeps() {
        // 14 unconditional activators; Citizens (+1) and WorldGuard (+2) are absent
        // in this test environment — see class-level Javadoc for details.
        new CoreExtension().onEnable(api);
        assertEquals(14, api.activators().keys().size(),
                "expected 14 unconditional core activator types "
                + "(17 in production with Citizens + WorldGuard present)");
    }

    @Test
    void coreExtension_registersExpectedCatalogs() {
        new CoreExtension().onEnable(api);
        assertEquals(6, api.catalogs().keys().size(),
                "expected 6 core catalog types (matches Task 12 migration count)");
    }

    @Test
    void coreExtension_canonicalActions_resolve() {
        new CoreExtension().onEnable(api);
        assertNotNull(api.actions().get("openMenu"),  "openMenu must be registered");
        assertNotNull(api.actions().get("closeMenu"), "closeMenu must be registered");
    }

    @Test
    void coreExtension_canonicalRules_resolve() {
        new CoreExtension().onEnable(api);
        assertNotNull(api.rules().get("permission"), "permission rule must be registered");
    }

    @Test
    void coreExtension_canonicalItemProperty_resolve() {
        new CoreExtension().onEnable(api);
        assertNotNull(api.itemProperties().get("material"), "material item-property must be registered");
    }

    // -------------------------------------------------------------------------
    // Minimal AbstractMenusApi stub backed by real TypeRegistryImpl instances.
    // Only the registry accessor methods are implemented; all others throw
    // UnsupportedOperationException to catch accidental calls.
    // -------------------------------------------------------------------------

    private static final class StubApi implements AbstractMenusApi {

        private final NodeSerializers serializers = NodeSerializers.defaults();

        private final TypeRegistry<Action>       actions        = new TypeRegistryImpl<>(serializers);
        private final TypeRegistry<Rule>         rules          = new TypeRegistryImpl<>(serializers);
        private final TypeRegistry<Activator>    activators     = new TypeRegistryImpl<>(serializers);
        private final TypeRegistry<ItemProperty> itemProperties = new TypeRegistryImpl<>(serializers);
        private final TypeRegistry<Catalog<?>>   catalogs       = new TypeRegistryImpl<>(serializers);
        private final ProviderRegistry           providers      = new ProviderRegistryImpl();

        @Override public TypeRegistry<Action>       actions()        { return actions; }
        @Override public TypeRegistry<Rule>         rules()          { return rules; }
        @Override public TypeRegistry<Activator>    activators()     { return activators; }
        @Override public TypeRegistry<ItemProperty> itemProperties() { return itemProperties; }
        @Override public TypeRegistry<Catalog<?>>   catalogs()       { return catalogs; }
        @Override public ProviderRegistry           providers()      { return providers; }
        @Override public NodeSerializers            serializers()    { return serializers; }

        @Override public VariableManager variables()   { throw new UnsupportedOperationException(); }
        @Override public org.bukkit.plugin.Plugin getPlugin() { throw new UnsupportedOperationException(); }
        @Override public void loadMenus()              { throw new UnsupportedOperationException(); }
        @Override public void openMenu(Activator a, Object ctx, org.bukkit.entity.Player p, Menu m) { throw new UnsupportedOperationException(); }
        @Override public void openMenu(org.bukkit.entity.Player p, Menu m) { throw new UnsupportedOperationException(); }
        @Override public Optional<Menu> getOpenedMenu(org.bukkit.entity.Player p) { throw new UnsupportedOperationException(); }
        @Override public String apiVersion()           { return "test"; }
    }

    /** Minimal extension for test purposes (not used directly here). */
    private static final class DummyExtension implements MenuExtension {
        @Override public String name()                              { return "TestExtension"; }
        @Override public void onEnable(AbstractMenusApi api)       {}
    }
}
