package ru.abstractmenus.addon;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AddonConfTest {

    @Test
    void parse_minimal_requiredOnly() {
        String hocon = """
                name = "MyAddon"
                version = "1.0.0"
                main = "com.example.MyAddon"
                """;
        AddonConf c = AddonConf.parse(hocon);

        assertEquals("MyAddon", c.name());
        assertEquals("1.0.0", c.version());
        assertEquals("com.example.MyAddon", c.main());
        assertEquals(List.of(), c.authors());
        assertEquals("", c.description());
        assertNull(c.targetApiVersion());
        assertEquals(List.of(), c.addonDependencies());
        assertEquals(List.of(), c.pluginDependencies());
        assertEquals(List.of(), c.pluginSoftDependencies());
    }

    @Test
    void parse_full() {
        String hocon = """
                name = "MyAddon"
                version = "1.2.3"
                main = "com.example.MyAddon"
                authors = ["alice", "bob"]
                description = "Does things."
                targetApiVersion = "2.0.0"
                addonDependencies = ["common-utils"]
                pluginDependencies = ["NBT-API"]
                pluginSoftDependencies = ["WorldGuard"]
                """;
        AddonConf c = AddonConf.parse(hocon);

        assertEquals(List.of("alice", "bob"), c.authors());
        assertEquals("Does things.", c.description());
        assertEquals("2.0.0", c.targetApiVersion());
        assertEquals(List.of("common-utils"), c.addonDependencies());
        assertEquals(List.of("NBT-API"), c.pluginDependencies());
        assertEquals(List.of("WorldGuard"), c.pluginSoftDependencies());
    }

    @Test
    void parse_missingName_throws() {
        String hocon = """
                version = "1.0.0"
                main = "com.example.MyAddon"
                """;
        AddonConfParseException ex = assertThrows(AddonConfParseException.class,
                () -> AddonConf.parse(hocon));
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
    }

    @Test
    void parse_missingVersion_throws() {
        String hocon = """
                name = "MyAddon"
                main = "com.example.MyAddon"
                """;
        assertThrows(AddonConfParseException.class, () -> AddonConf.parse(hocon));
    }

    @Test
    void parse_missingMain_throws() {
        String hocon = """
                name = "MyAddon"
                version = "1.0.0"
                """;
        assertThrows(AddonConfParseException.class, () -> AddonConf.parse(hocon));
    }

    @Test
    void parse_blankName_throws() {
        String hocon = """
                name = "   "
                version = "1.0.0"
                main = "com.example.MyAddon"
                """;
        assertThrows(AddonConfParseException.class, () -> AddonConf.parse(hocon));
    }

    @Test
    void parse_malformedHocon_throws() {
        String hocon = "this is not { valid hocon";
        assertThrows(AddonConfParseException.class, () -> AddonConf.parse(hocon));
    }

    @Test
    void listFields_acceptSingleElementInsteadOfList() {
        // HOCON allows either a single string or a list; both should parse.
        String hocon = """
                name = "MyAddon"
                version = "1.0.0"
                main = "com.example.MyAddon"
                authors = "solo"
                """;
        AddonConf c = AddonConf.parse(hocon);
        assertEquals(List.of("solo"), c.authors());
    }
}
