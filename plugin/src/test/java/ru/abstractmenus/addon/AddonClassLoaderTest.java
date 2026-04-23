package ru.abstractmenus.addon;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.abstractmenus.api.MenuExtension;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class AddonClassLoaderTest {

    @Test
    void parentFirstForApiPackage(@TempDir Path tmp) throws Exception {
        // A jar that SHIPS its own copy of a class in ru.abstractmenus.api.*.
        // The classloader must still prefer the parent's copy.
        File jar = tmp.resolve("addon.jar").toFile();
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jar))) {
            // Use the real parent class's bytes (we just want the jar to contain
            // the path). The key point is: the loader returns the PARENT's
            // Class<?>, not a freshly-defined one from the jar.
            byte[] bytes = classBytes(MenuExtension.class);
            out.putNextEntry(new JarEntry("ru/abstractmenus/api/MenuExtension.class"));
            out.write(bytes);
            out.closeEntry();
        }

        try (AddonClassLoader cl = new AddonClassLoader(
                new URL[]{jar.toURI().toURL()},
                AddonClassLoaderTest.class.getClassLoader())) {

            Class<?> loaded = cl.loadClass("ru.abstractmenus.api.MenuExtension");
            assertSame(MenuExtension.class, loaded,
                    "parent-first must return the parent's Class object");
        }
    }

    @Test
    void childFirstForEverythingElse(@TempDir Path tmp) throws Exception {
        // SampleAddonClass is a static inner class; its binary name contains '$'.
        String binaryName = SampleAddonClass.class.getName(); // ru.abstractmenus.addon.AddonClassLoaderTest$SampleAddonClass
        String entryPath  = binaryName.replace('.', '/') + ".class";

        File jar = tmp.resolve("addon.jar").toFile();
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jar))) {
            byte[] bytes = classBytes(SampleAddonClass.class);
            out.putNextEntry(new JarEntry(entryPath));
            out.write(bytes);
            out.closeEntry();
        }

        try (AddonClassLoader cl = new AddonClassLoader(
                new URL[]{jar.toURI().toURL()},
                AddonClassLoaderTest.class.getClassLoader())) {

            Class<?> loaded = cl.loadClass(binaryName);
            assertEquals(binaryName, loaded.getName());
        }
    }

    @Test
    void missingClass_throwsClassNotFound(@TempDir Path tmp) throws Exception {
        File jar = tmp.resolve("empty.jar").toFile();
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jar))) {
            // empty jar
        }

        try (AddonClassLoader cl = new AddonClassLoader(
                new URL[]{jar.toURI().toURL()},
                AddonClassLoaderTest.class.getClassLoader())) {

            assertThrows(ClassNotFoundException.class,
                    () -> cl.loadClass("com.nowhere.Missing"));
        }
    }

    // --- helpers ---

    private static byte[] classBytes(Class<?> cls) throws Exception {
        String path = cls.getName().replace('.', '/') + ".class";
        try (InputStream in = cls.getClassLoader().getResourceAsStream(path);
             ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            if (in == null) throw new IllegalStateException("Cannot find " + path);
            in.transferTo(buf);
            return buf.toByteArray();
        }
    }

    /** Public fixture class — a test jar will pack its bytecode. */
    public static class SampleAddonClass {
        public String ping() { return "pong"; }
    }
}
