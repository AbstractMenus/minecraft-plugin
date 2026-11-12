package ru.abstractmenus.addon;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.testsupport.ApiTestSupport;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class TestAddonRegressions {
    public static class Failing implements MenuExtension {
        public void onEnable(AbstractMenusApi api) { throw new IllegalStateException("fixture failure"); }
    }

    public static class Base implements MenuExtension {
        public void onEnable(AbstractMenusApi api) { }
    }

    public static class Dependent implements MenuExtension {
        public void onEnable(AbstractMenusApi api) { api.rules().register("dependent",SampleRule.class,new SampleRule.Serializer(),this); }
    }

    public static class SampleRule implements Rule {
        public boolean check(org.bukkit.entity.Player player,ru.abstractmenus.api.inventory.Menu menu,ru.abstractmenus.api.inventory.Item item) { return true; }
        public static class Serializer implements NodeSerializer<SampleRule> {
            public SampleRule deserialize(Class<SampleRule> type,ConfigNode node) { return new SampleRule(); }
        }
    }

    private void jar(Path path,String name,Class<?> main,String dependency,boolean shadeHocon) throws Exception {
        try (var out = new JarOutputStream(Files.newOutputStream(path))) {
            out.putNextEntry(new JarEntry("addon.conf"));
            out.write(("name=\""+name+"\"\nversion=\"1\"\nmain=\""+main.getName()+"\"\n"
                    +(dependency == null ? "" : "addonDependencies=[\""+dependency+"\"]\n")).getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
            for (Class<?> type : List.of(main,SampleRule.class,SampleRule.Serializer.class)) {
                String resource = type.getName().replace('.','/')+".class";
                out.putNextEntry(new JarEntry(resource));
                try (var in = type.getClassLoader().getResourceAsStream(resource)) { in.transferTo(out); }
                out.closeEntry();
            }
            if (shadeHocon) {
                try (var source = new JarFile(Path.of(NodeSerializer.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile())) {
                    for (var entry : Collections.list(source.entries())) {
                        if (entry.isDirectory() || !entry.getName().startsWith("ru/abstractmenus/hocon/")) continue;
                        out.putNextEntry(new JarEntry(entry.getName()));
                        try (var in = source.getInputStream(entry)) { in.transferTo(out); }
                        out.closeEntry();
                    }
                }
            }
        }
    }

    @Test
    void startupDoesNotEnableDependentsOfFailedAddons(@TempDir Path folder) throws Exception {
        ru.abstractmenus.api.Logger.set(java.util.logging.Logger.getLogger("addon-test"));
        jar(folder.resolve("base.jar"),"Base",Failing.class,null,false);
        jar(folder.resolve("bridge.jar"),"Bridge",Dependent.class,"Base",false);
        jar(folder.resolve("leaf.jar"),"Leaf",Dependent.class,"Bridge",false);
        try (var support = ApiTestSupport.install()) {
            var manager = new AddonManager(folder,support.api());
            try {
                manager.loadAll();
                assertEquals(AddonStatus.FAILED,manager.get("Base").orElseThrow().getStatus());
                assertEquals(AddonStatus.FAILED,manager.get("Bridge").orElseThrow().getStatus());
                assertEquals(AddonStatus.FAILED,manager.get("Leaf").orElseThrow().getStatus());
                assertNull(support.api().rules().get("dependent"));
            } finally { manager.unloadAll(); }
        }
    }

    @Test
    void shadedHoconUsesSharedApiTypes(@TempDir Path folder) throws Exception {
        ru.abstractmenus.api.Logger.set(java.util.logging.Logger.getLogger("addon-test"));
        jar(folder.resolve("addon.jar"),"Shaded",Dependent.class,null,true);
        try (var support = ApiTestSupport.install()) {
            var manager = new AddonManager(folder,support.api());
            try {
                manager.loadAll();
                var addon = manager.get("Shaded").orElseThrow();
                assertEquals(AddonStatus.ENABLED,addon.getStatus(),String.valueOf(addon.getError()));
                assertNotNull(support.api().rules().get("dependent"));
                assertSame(NodeSerializer.class,addon.getClassLoader().loadClass(NodeSerializer.class.getName()));
            } finally { manager.unloadAll(); }
        }
    }

    @Test
    void rescanEnablesNewDependenciesBeforeDependents(@TempDir Path folder) throws Exception {
        ru.abstractmenus.api.Logger.set(java.util.logging.Logger.getLogger("addon-test"));
        Files.createFile(folder.resolve("one.jar")); Files.createFile(folder.resolve("two.jar"));
        List<Path> order;
        try (var entries = Files.newDirectoryStream(folder,"*.jar")) {
            order = new ArrayList<>(); entries.forEach(order::add);
        }
        jar(order.get(0),"Bridge",Dependent.class,"Base",false);
        jar(order.get(1),"Base",Base.class,null,false);
        try (var support = ApiTestSupport.install()) {
            var manager = new AddonManager(folder,support.api());
            try {
                assertEquals(2,manager.rescan().size());
                assertEquals(AddonStatus.ENABLED,manager.get("Base").orElseThrow().getStatus());
                assertEquals(AddonStatus.ENABLED,manager.get("Bridge").orElseThrow().getStatus());
                assertNotNull(support.api().rules().get("dependent"));
                jar(folder.resolve("later.jar"),"Later",Base.class,"Base",false);
                assertEquals(1,manager.rescan().size());
                assertEquals(AddonStatus.ENABLED,manager.get("Later").orElseThrow().getStatus());
            } finally { manager.unloadAll(); }
        }
    }
}
