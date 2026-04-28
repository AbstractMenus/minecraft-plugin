package ru.abstractmenus.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.api.handler.EconomyHandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ProviderRegistryImplTest {

    private ProviderRegistryImpl registry;
    private MenuExtension ownerA;
    private MenuExtension ownerB;

    @BeforeEach
    void setUp() {
        registry = new ProviderRegistryImpl();
        ownerA = new DummyExtension("A");
        ownerB = new DummyExtension("B");
    }

    @Test
    void register_singleProvider_resolvesByIdAndAuto() {
        EconomyHandler vault = mock(EconomyHandler.class);
        registry.economy().register("vault", vault, 50, ownerA);

        assertSame(vault, registry.economy().resolve());
        assertSame(vault, registry.economy().resolve("vault"));
        assertEquals(1, registry.economy().all().size());
        assertTrue(registry.economy().has("vault"));
    }

    @Test
    void auto_returnsNullWhenEmpty() {
        assertNull(registry.economy().resolve());
        assertFalse(registry.economy().has("anything"));
    }

    @Test
    void auto_highestPriorityWins() {
        EconomyHandler vault = mock(EconomyHandler.class);
        EconomyHandler pp = mock(EconomyHandler.class);
        registry.economy().register("vault", vault, 50, ownerA);
        registry.economy().register("playerpoints", pp, 100, ownerA);

        assertSame(pp, registry.economy().resolve());
    }

    @Test
    void auto_tieBreaksToFirstRegistered() {
        EconomyHandler a = mock(EconomyHandler.class);
        EconomyHandler b = mock(EconomyHandler.class);
        registry.economy().register("alpha", a, 50, ownerA);
        registry.economy().register("beta",  b, 50, ownerA);

        assertSame(a, registry.economy().resolve());
    }

    @Test
    void lookupById_caseInsensitive() {
        EconomyHandler vault = mock(EconomyHandler.class);
        registry.economy().register("Vault", vault, 50, ownerA);

        assertSame(vault, registry.economy().resolve("VAULT"));
        assertSame(vault, registry.economy().resolve("vault"));
        assertTrue(registry.economy().has("vAuLt"));
    }

    @Test
    void unregisterAll_removesOnlyThatOwner() {
        EconomyHandler ea = mock(EconomyHandler.class);
        EconomyHandler eb = mock(EconomyHandler.class);
        registry.economy().register("a", ea, 50, ownerA);
        registry.economy().register("b", eb, 50, ownerB);

        registry.unregisterAll(ownerA);

        assertNull(registry.economy().resolve("a"));
        assertSame(eb, registry.economy().resolve("b"));
        assertEquals(1, registry.economy().all().size());
    }

    @Test
    void overwrite_replacesPrevious() {
        EconomyHandler old = mock(EconomyHandler.class);
        EconomyHandler fresh = mock(EconomyHandler.class);
        registry.economy().register("vault", old, 50, ownerA);
        registry.economy().register("vault", fresh, 50, ownerB);

        assertSame(fresh, registry.economy().resolve("vault"));
    }

    @Test
    void sectionsAreIndependent() {
        EconomyHandler e = mock(EconomyHandler.class);
        registry.economy().register("e", e, 50, ownerA);

        assertNull(registry.permissions().resolve());
        assertNull(registry.levels().resolve());
        assertNull(registry.placeholders().resolve());
        assertNull(registry.skins().resolve());
    }

    @Test
    void configDefault_prefersConfiguredId() {
        EconomyHandler vault = mock(EconomyHandler.class);
        EconomyHandler pp = mock(EconomyHandler.class);
        registry.economy().register("vault", vault, 50, ownerA);
        registry.economy().register("playerpoints", pp, 100, ownerA);

        // Without config, auto prefers playerpoints (priority 100).
        assertSame(pp, registry.economy().resolve());

        // With config override to vault, vault wins despite lower priority.
        registry.setConfigDefaults(kind -> "economy".equals(kind) ? "vault" : null);
        assertSame(vault, registry.economy().resolve());
    }

    @Test
    void configDefault_autoKeyword_fallsBackToAuto() {
        EconomyHandler vault = mock(EconomyHandler.class);
        registry.economy().register("vault", vault, 50, ownerA);

        registry.setConfigDefaults(kind -> "auto");
        assertSame(vault, registry.economy().resolve());
    }

    @Test
    void configDefault_unknownId_fallsBackToAuto() {
        EconomyHandler vault = mock(EconomyHandler.class);
        registry.economy().register("vault", vault, 50, ownerA);

        registry.setConfigDefaults(kind -> "ghost");  // not registered
        assertSame(vault, registry.economy().resolve());  // auto fallback
    }

    // --- helper ---

    static class DummyExtension implements MenuExtension {
        private final String name;
        DummyExtension(String name) { this.name = name; }
        @Override public void onEnable(AbstractMenusApi api) {}
        @Override public String name() { return name; }
    }
}
