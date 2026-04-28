package ru.abstractmenus.testsupport;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicesManager;
import org.mockito.MockedStatic;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.Catalog;
import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.api.ProviderRegistry;
import ru.abstractmenus.impl.ProviderRegistryImpl;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.TypeRegistry;
import ru.abstractmenus.impl.TypeRegistryImpl;
import ru.abstractmenus.api.handler.PlaceholderHandler;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.variables.VariableManager;
import ru.abstractmenus.hocon.api.serialize.NodeSerializers;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Helper for unit tests that exercise production code calling
 * {@code AbstractMenusApi.get().providers().*()}.
 *
 * <p>Stubs the static {@link Bukkit#getServicesManager()} with a real
 * {@link ServicesManager} mock that returns a lightweight {@link StubApi}
 * backed by a real {@link ProviderRegistryImpl}. Tests can register the
 * stub handlers they need directly on the registry.
 *
 * <p>Typical usage:
 * <pre>{@code
 * private static ApiTestSupport support;
 *
 * @BeforeAll
 * static void setUp() {
 *     support = ApiTestSupport.install();
 *     support.providers().placeholders().register("test", myHandler, 100, support.owner());
 * }
 *
 * @AfterAll
 * static void tearDown() { support.close(); }
 * }</pre>
 */
public final class ApiTestSupport implements AutoCloseable {

    private final MockedStatic<Bukkit> bukkitMock;
    private final StubApi api;
    private final TestOwner owner;

    private ApiTestSupport(MockedStatic<Bukkit> bukkitMock, StubApi api, TestOwner owner) {
        this.bukkitMock = bukkitMock;
        this.api = api;
        this.owner = owner;
    }

    public static ApiTestSupport install() {
        StubApi api = new StubApi();

        ServicesManager servicesManager = mock(ServicesManager.class);
        when(servicesManager.load(AbstractMenusApi.class)).thenReturn(api);

        MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class);
        bukkitMock.when(Bukkit::getServicesManager).thenReturn(servicesManager);

        return new ApiTestSupport(bukkitMock, api, new TestOwner());
    }

    public ProviderRegistry providers() { return api.providers(); }
    public MenuExtension owner() { return owner; }
    public AbstractMenusApi api() { return api; }

    /** Shortcut: register {@code handler} as the test placeholder provider. */
    public void installPlaceholderHandler(PlaceholderHandler handler) {
        api.providers().placeholders().register("test", handler, 100, owner);
    }

    @Override
    public void close() {
        bukkitMock.close();
    }

    // -------------------------------------------------------------------

    private static final class TestOwner implements MenuExtension {
        @Override public String name() { return "ApiTestSupport"; }
        @Override public void onEnable(AbstractMenusApi api) {}
    }

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

        @Override public VariableManager variables() { throw new UnsupportedOperationException(); }
        @Override public org.bukkit.plugin.Plugin getPlugin() { throw new UnsupportedOperationException(); }
        @Override public void loadMenus()             { throw new UnsupportedOperationException(); }
        @Override public void openMenu(Activator a, Object ctx, org.bukkit.entity.Player p, Menu m) { throw new UnsupportedOperationException(); }
        @Override public void openMenu(org.bukkit.entity.Player p, Menu m) { throw new UnsupportedOperationException(); }
        @Override public Optional<Menu> getOpenedMenu(org.bukkit.entity.Player p) { throw new UnsupportedOperationException(); }
        @Override public String apiVersion()          { return "test"; }
    }
}
