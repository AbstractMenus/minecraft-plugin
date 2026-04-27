package ru.abstractmenus.addon;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.hocon.api.serialize.NodeSerializers;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for {@link AddonManager}: builds a real addon
 * jar at test-time via {@link JarOutputStream}, feeds it through
 * {@code loadAll()}, verifies the addon's action is registered, then verifies
 * {@code unloadAll()} strips the registration.
 *
 * <p>Uses a pure-unit {@link StubApi} + the package-private test-only
 * {@code AddonManager(Path, AbstractMenusApi)} constructor, since MockBukkit
 * does not work with Paper 1.21.11 (see {@code CoreExtensionTest}).
 */
class AddonManagerIntegrationTest {

    @BeforeAll
    static void initLogger() {
        // AddonManager uses ru.abstractmenus.api.Logger, which delegates to a
        // static java.util.logging.Logger set by the plugin at runtime. In the
        // test environment we must inject one ourselves or any logging path
        // inside loadAll will NPE.
        ru.abstractmenus.api.Logger.set(
                java.util.logging.Logger.getLogger("AddonManagerIntegrationTest"));
    }

    /** Test fixture — a MenuExtension that registers one action. */
    public static class TestAddon implements MenuExtension {
        @Override public void onEnable(AbstractMenusApi api) {
            api.actions().register("testPing", PingAction.class,
                    new PingAction.Serializer(), this);
        }
        @Override public String name() { return "TestAddon"; }
        @Override public String version() { return "1.0.0"; }
    }

    /** Test fixture — a plain Action. */
    public static class PingAction implements Action {
        @Override
        public void activate(org.bukkit.entity.Player player,
                             ru.abstractmenus.api.inventory.Menu menu,
                             ru.abstractmenus.api.inventory.Item clickedItem) {
            // no-op
        }

        public static class Serializer implements NodeSerializer<PingAction> {
            @Override
            public PingAction deserialize(Class<PingAction> type, ConfigNode node)
                    throws NodeSerializeException {
                return new PingAction();
            }
        }
    }

    @Test
    void loadAll_readsJar_enablesAddon_registersAction(@TempDir Path serverRoot) throws Exception {
        // Layout: <tmp>/plugins/AbstractMenus/addons/
        Path dataFolder = Files.createDirectories(
                serverRoot.resolve("plugins").resolve("AbstractMenus"));
        Path addonsDir = Files.createDirectories(dataFolder.resolve("addons"));

        // Pack TestAddon + PingAction + its Serializer + addon.conf into a jar.
        Path jar = addonsDir.resolve("test-addon.jar");
        String hocon = """
                name = "TestAddon"
                version = "1.0.0"
                main = "%s"
                """.formatted(TestAddon.class.getName());

        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jar.toFile()))) {
            out.putNextEntry(new JarEntry("addon.conf"));
            out.write(hocon.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
            packClass(out, TestAddon.class);
            packClass(out, PingAction.class);
            packClass(out, PingAction.Serializer.class);
        }

        // Build a minimal AbstractMenusApi stub backed by real registries.
        StubApi api = new StubApi();

        // Use the test-only constructor.
        AddonManager manager = new AddonManager(addonsDir, api);
        manager.loadAll();

        // Verify: exactly one addon is loaded, with ENABLED status.
        assertEquals(1, manager.loaded().size());
        LoadedAddon loaded = manager.loaded().iterator().next();
        assertEquals("TestAddon", loaded.getConf().name());
        assertEquals(AddonStatus.ENABLED, loaded.getStatus(),
                "addon must reach ENABLED status (error: "
                        + (loaded.getError() == null ? "none" : loaded.getError()) + ")");

        // Verify: the action is registered.
        assertNotNull(api.actions().get("testPing"),
                "TestAddon must register action 'testPing'");

        // Verify: unload cleans up.
        manager.unloadAll();
        assertNull(api.actions().get("testPing"),
                "unloadAll should strip the addon's registrations");
        assertEquals(0, manager.loaded().size());
    }

    // --- helpers ---

    private static void packClass(JarOutputStream out, Class<?> cls) throws Exception {
        String path = cls.getName().replace('.', '/') + ".class";
        try (InputStream in = cls.getClassLoader().getResourceAsStream(path);
             ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            if (in == null) throw new IllegalStateException("cannot find " + path);
            in.transferTo(buf);
            out.putNextEntry(new JarEntry(path));
            out.write(buf.toByteArray());
            out.closeEntry();
        }
    }

    /** Minimal AbstractMenusApi — real registries, stub lifecycle. */
    private static class StubApi implements AbstractMenusApi {
        private final NodeSerializers serializers = NodeSerializers.defaults();
        private final TypeRegistry<Action>        actions        = new TypeRegistryImpl<>(serializers);
        private final TypeRegistry<Rule>          rules          = new TypeRegistryImpl<>(serializers);
        private final TypeRegistry<Activator>     activators     = new TypeRegistryImpl<>(serializers);
        private final TypeRegistry<ItemProperty>  itemProperties = new TypeRegistryImpl<>(serializers);
        private final TypeRegistry<Catalog<?>>    catalogs       = new TypeRegistryImpl<>(serializers);
        private final ProviderRegistry            providers      = new ProviderRegistryImpl();

        @Override public TypeRegistry<Action>        actions()        { return actions; }
        @Override public TypeRegistry<Rule>          rules()          { return rules; }
        @Override public TypeRegistry<Activator>     activators()     { return activators; }
        @Override public TypeRegistry<ItemProperty>  itemProperties() { return itemProperties; }
        @Override public TypeRegistry<Catalog<?>>    catalogs()       { return catalogs; }
        @Override public ProviderRegistry            providers()      { return providers; }
        @Override public NodeSerializers serializers() { return serializers; }
        @Override public VariableManager variables() { return null; }
        @Override public org.bukkit.plugin.Plugin getPlugin() { return null; }
        @Override public void loadMenus() {}
        @Override public void openMenu(Activator activator, Object ctx, org.bukkit.entity.Player player, Menu menu) {}
        @Override public void openMenu(org.bukkit.entity.Player player, Menu menu) {}
        @Override public Optional<Menu> getOpenedMenu(org.bukkit.entity.Player player) { return Optional.empty(); }
        @Override public String apiVersion() { return "test"; }
    }
}
