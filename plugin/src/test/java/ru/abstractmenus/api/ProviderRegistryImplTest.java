package ru.abstractmenus.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        registry.registerEconomy("vault", vault, 50, ownerA);

        assertSame(vault, registry.economy());
        assertSame(vault, registry.economy("vault"));
        assertEquals(1, registry.allEconomy().size());
        assertTrue(registry.hasEconomy("vault"));
    }

    @Test
    void auto_returnsNullWhenEmpty() {
        assertNull(registry.economy());
        assertFalse(registry.hasEconomy("anything"));
    }

    @Test
    void auto_highestPriorityWins() {
        EconomyHandler vault = mock(EconomyHandler.class);
        EconomyHandler pp = mock(EconomyHandler.class);
        registry.registerEconomy("vault", vault, 50, ownerA);
        registry.registerEconomy("playerpoints", pp, 100, ownerA);

        assertSame(pp, registry.economy());
    }

    @Test
    void auto_tieBreaksToFirstRegistered() {
        EconomyHandler a = mock(EconomyHandler.class);
        EconomyHandler b = mock(EconomyHandler.class);
        registry.registerEconomy("alpha", a, 50, ownerA);
        registry.registerEconomy("beta",  b, 50, ownerA);

        assertSame(a, registry.economy());
    }

    @Test
    void lookupById_caseInsensitive() {
        EconomyHandler vault = mock(EconomyHandler.class);
        registry.registerEconomy("Vault", vault, 50, ownerA);

        assertSame(vault, registry.economy("VAULT"));
        assertSame(vault, registry.economy("vault"));
        assertTrue(registry.hasEconomy("vAuLt"));
    }

    @Test
    void unregisterAll_removesOnlyThatOwner() {
        EconomyHandler ea = mock(EconomyHandler.class);
        EconomyHandler eb = mock(EconomyHandler.class);
        registry.registerEconomy("a", ea, 50, ownerA);
        registry.registerEconomy("b", eb, 50, ownerB);

        registry.unregisterAll(ownerA);

        assertNull(registry.economy("a"));
        assertSame(eb, registry.economy("b"));
        assertEquals(1, registry.allEconomy().size());
    }

    @Test
    void overwrite_replacesPrevious() {
        EconomyHandler old = mock(EconomyHandler.class);
        EconomyHandler fresh = mock(EconomyHandler.class);
        registry.registerEconomy("vault", old, 50, ownerA);
        registry.registerEconomy("vault", fresh, 50, ownerB);

        assertSame(fresh, registry.economy("vault"));
    }

    @Test
    void sectionsAreIndependent() {
        EconomyHandler e = mock(EconomyHandler.class);
        registry.registerEconomy("e", e, 50, ownerA);

        assertNull(registry.permissions());
        assertNull(registry.levels());
        assertNull(registry.placeholders());
        assertNull(registry.skins());
    }

    @Test
    void configDefault_prefersConfiguredId() {
        EconomyHandler vault = mock(EconomyHandler.class);
        EconomyHandler pp = mock(EconomyHandler.class);
        registry.registerEconomy("vault", vault, 50, ownerA);
        registry.registerEconomy("playerpoints", pp, 100, ownerA);

        // Without config, auto prefers playerpoints (priority 100).
        assertSame(pp, registry.economy());

        // With config override to vault, vault wins despite lower priority.
        registry.setConfigDefaults(kind -> "economy".equals(kind) ? "vault" : null);
        assertSame(vault, registry.economy());
    }

    @Test
    void configDefault_autoKeyword_fallsBackToAuto() {
        EconomyHandler vault = mock(EconomyHandler.class);
        registry.registerEconomy("vault", vault, 50, ownerA);

        registry.setConfigDefaults(kind -> "auto");
        assertSame(vault, registry.economy());
    }

    @Test
    void configDefault_unknownId_fallsBackToAuto() {
        EconomyHandler vault = mock(EconomyHandler.class);
        registry.registerEconomy("vault", vault, 50, ownerA);

        registry.setConfigDefaults(kind -> "ghost");  // not registered
        assertSame(vault, registry.economy());  // auto fallback
    }

    // --- helper ---

    static class DummyExtension implements MenuExtension {
        private final String name;
        DummyExtension(String name) { this.name = name; }
        @Override public void onEnable(AbstractMenusApi api) {}
        @Override public String name() { return name; }
    }
}
