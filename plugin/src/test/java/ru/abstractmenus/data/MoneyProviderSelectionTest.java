package ru.abstractmenus.data;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.abstractmenus.api.handler.EconomyHandler;
import ru.abstractmenus.data.actions.ActionMoneyGive;
import ru.abstractmenus.data.actions.ActionMoneyTake;
import ru.abstractmenus.data.rules.RuleMoney;
import ru.abstractmenus.datatype.TypeDouble;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.source.ConfigSources;
import ru.abstractmenus.testsupport.ApiTestSupport;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * End-to-end tests for provider: selection in ActionMoneyTake / ActionMoneyGive / RuleMoney.
 *
 * <p>Covers:
 * <ol>
 *   <li>Explicit {@code provider: "vault"} routes to the named handler.</li>
 *   <li>Omitted provider auto-resolves to the highest-priority handler.</li>
 *   <li>Scalar (legacy) form {@code takeMoney: 100.0} still deserializes.</li>
 *   <li>Unknown provider id throws {@link NodeSerializeException} at deserialize time.</li>
 * </ol>
 */
class MoneyProviderSelectionTest {

    /** vault: priority 50, pp: priority 100 — pp wins auto-resolve. */
    private static final int VAULT_PRIORITY = 50;
    private static final int PP_PRIORITY    = 100;

    private ApiTestSupport support;
    private EconomyHandler vault;
    private EconomyHandler pp;

    @BeforeEach
    void setUp() {
        support = ApiTestSupport.install();

        vault = mock(EconomyHandler.class);
        pp    = mock(EconomyHandler.class);

        // Register TypeDouble.Serializer so the HOCON deserialization pipeline
        // can resolve 'amount' nodes. In production this is done in Serializers.init().
        support.api().serializers().register(TypeDouble.class, new TypeDouble.Serializer());

        support.providers().economy().register("vault",        vault, VAULT_PRIORITY, support.owner());
        support.providers().economy().register("playerpoints", pp,    PP_PRIORITY,    support.owner());
    }

    @AfterEach
    void tearDown() {
        support.close();
    }

    // -------------------------------------------------------------------------
    // ActionMoneyTake
    // -------------------------------------------------------------------------

    @Test
    void take_explicitProvider_usesNamedHandler() throws Exception {
        String hocon = "amount = 100\nprovider = \"vault\"";
        ActionMoneyTake action = new ActionMoneyTake.Serializer()
                .deserialize(ActionMoneyTake.class, parseMap(hocon));

        Player player = mock(Player.class);
        action.activate(player, null, null);

        verify(vault).takeBalance(eq(player), eq(100.0));
        verify(pp, never()).takeBalance(any(), anyDouble());
    }

    @Test
    void take_omittedProvider_usesHighestPriority() throws Exception {
        // map form without provider field → auto-resolve → pp (priority 100 > 50)
        String hocon = "amount = 50";
        ActionMoneyTake action = new ActionMoneyTake.Serializer()
                .deserialize(ActionMoneyTake.class, parseMap(hocon));

        Player player = mock(Player.class);
        action.activate(player, null, null);

        verify(pp).takeBalance(eq(player), eq(50.0));
        verify(vault, never()).takeBalance(any(), anyDouble());
    }

    @Test
    void take_scalarForm_backwardCompat() throws Exception {
        // Legacy scalar: takeMoney: 100.0 — the node is a bare number, not a map.
        ActionMoneyTake action = new ActionMoneyTake.Serializer()
                .deserialize(ActionMoneyTake.class, parseScalar("100.0"));

        Player player = mock(Player.class);
        action.activate(player, null, null);

        // Scalar form → no explicit provider → auto-resolve → pp
        verify(pp).takeBalance(eq(player), eq(100.0));
    }

    @Test
    void take_unknownProvider_throwsAtDeserialize() {
        String hocon = "amount = 100\nprovider = \"ghost\"";
        var serializer = new ActionMoneyTake.Serializer();

        NodeSerializeException ex = assertThrows(NodeSerializeException.class,
                () -> serializer.deserialize(ActionMoneyTake.class, parseMap(hocon)));
        assertTrue(ex.getMessage().toLowerCase().contains("ghost"),
                "error message should mention the unknown provider id; got: " + ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // ActionMoneyGive
    // -------------------------------------------------------------------------

    @Test
    void give_explicitProvider_usesNamedHandler() throws Exception {
        String hocon = "amount = 200\nprovider = \"playerpoints\"";
        ActionMoneyGive action = new ActionMoneyGive.Serializer()
                .deserialize(ActionMoneyGive.class, parseMap(hocon));

        Player player = mock(Player.class);
        action.activate(player, null, null);

        verify(pp).giveBalance(eq(player), eq(200.0));
        verify(vault, never()).giveBalance(any(), anyDouble());
    }

    @Test
    void give_unknownProvider_throwsAtDeserialize() {
        String hocon = "amount = 200\nprovider = \"nowhere\"";
        var serializer = new ActionMoneyGive.Serializer();

        NodeSerializeException ex = assertThrows(NodeSerializeException.class,
                () -> serializer.deserialize(ActionMoneyGive.class, parseMap(hocon)));
        assertTrue(ex.getMessage().toLowerCase().contains("nowhere"),
                "error message should mention the unknown provider id; got: " + ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // RuleMoney
    // -------------------------------------------------------------------------

    @Test
    void rule_explicitProvider_checksNamedHandler() throws Exception {
        when(vault.hasBalance(any(), anyDouble())).thenReturn(true);
        when(pp.hasBalance(any(), anyDouble())).thenReturn(false);

        String hocon = "amount = 500\nprovider = \"vault\"";
        RuleMoney rule = new RuleMoney.Serializer()
                .deserialize(RuleMoney.class, parseMap(hocon));

        Player player = mock(Player.class);
        boolean result = rule.check(player, null, null);

        assertTrue(result, "vault says true — rule should pass");
        verify(vault).hasBalance(eq(player), eq(500.0));
        verify(pp, never()).hasBalance(any(), anyDouble());
    }

    @Test
    void rule_unknownProvider_throwsAtDeserialize() {
        String hocon = "amount = 100\nprovider = \"missing\"";
        var serializer = new RuleMoney.Serializer();

        NodeSerializeException ex = assertThrows(NodeSerializeException.class,
                () -> serializer.deserialize(RuleMoney.class, parseMap(hocon)));
        assertTrue(ex.getMessage().toLowerCase().contains("missing"),
                "error message should mention the unknown provider id; got: " + ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // Parse helpers — use the test-support serializers so TypeDouble is known.
    // -------------------------------------------------------------------------

    /**
     * Parse a HOCON snippet that represents a map (key = value pairs) into a
     * ConfigNode using the same {@link ru.abstractmenus.hocon.api.serialize.NodeSerializers}
     * instance that has {@code TypeDouble} registered.
     */
    private ConfigNode parseMap(String hocon) throws Exception {
        String wrapped = "val {\n" + hocon + "\n}";
        byte[] bytes = wrapped.getBytes(StandardCharsets.UTF_8);
        ConfigNode root = ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", new ByteArrayInputStream(bytes)))
                .serializers(support.api().serializers())
                .build()
                .load();
        return root.node("val");
    }

    /**
     * Parse a bare scalar value (e.g. {@code "100.0"}) into a ConfigNode that
     * represents that scalar — mirrors how the legacy takeMoney: 100 form
     * arrives at the Serializer (the node IS the value, not a map).
     */
    private ConfigNode parseScalar(String scalar) throws Exception {
        String wrapped = "val = " + scalar;
        byte[] bytes = wrapped.getBytes(StandardCharsets.UTF_8);
        ConfigNode root = ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", new ByteArrayInputStream(bytes)))
                .serializers(support.api().serializers())
                .build()
                .load();
        return root.node("val");
    }
}
