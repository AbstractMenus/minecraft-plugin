package ru.abstractmenus.data.actions;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.abstractmenus.api.handler.PlaceholderHandler;
import ru.abstractmenus.testsupport.ApiTestSupport;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.source.ConfigSources;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Regression tests for {@link ActionCommand}.
 * Pins the Bukkit API contract so the cmd dispatch cannot regress back to
 * {@code player.chat("/" + cmd)} (which creates a chat event cycle + double
 * placeholder replacement).
 */
class TestActionCommandBehavior {

    private static ApiTestSupport apiSupport;

    @BeforeAll
    static void installIdentityHandler() {
        apiSupport = ApiTestSupport.install();
        apiSupport.installPlaceholderHandler(new PlaceholderHandler() {
            @Override public String replacePlaceholder(Player p, String s) { return s; }
            @Override public String replace(Player p, String s) { return s; }
            @Override public List<String> replace(Player p, List<String> l) { return l; }
            @Override public void registerAll() {}
        });
    }

    @AfterAll
    static void restore() {
        apiSupport.close();
    }

    @Test
    void activateRoutesPlayerCommandsThroughPerformCommand() throws Exception {
        ActionCommand action = buildAction("{ player = \"spawn\", ignorePlaceholder = true }");
        Player player = mock(Player.class);

        action.activate(player, null, null);

        verify(player).performCommand("spawn");
        verify(player, never()).chat(anyString());
    }

    @Test
    void activateDispatchesEachPlayerCommandIndividually() throws Exception {
        ActionCommand action = buildAction("{ player = [\"first\", \"second\", \"third\"], ignorePlaceholder = true }");
        Player player = mock(Player.class);

        action.activate(player, null, null);

        ArgumentCaptor<String> sent = ArgumentCaptor.forClass(String.class);
        verify(player, times(3)).performCommand(sent.capture());
        org.junit.jupiter.api.Assertions.assertEquals(
                List.of("first", "second", "third"), sent.getAllValues());
    }

    @Test
    void activateDoesNotPrependSlash() throws Exception {
        // performCommand expects the command WITHOUT a leading slash.
        ActionCommand action = buildAction("{ player = \"tp ~ ~ ~\", ignorePlaceholder = true }");
        Player player = mock(Player.class);

        action.activate(player, null, null);

        verify(player).performCommand(argThat(s -> !s.startsWith("/")));
    }

    @Test
    void placeholderReplacementRunsExactlyOnce() throws Exception {
        // The old impl called replace(...) twice ("replace(replace(...))"), the
        // new one replaces once. Install a counting handler to enforce.
        int[] callCount = {0};
        // Register a higher-priority counting handler that preempts the identity
        // one installed in @BeforeAll; unregister it in the finally block so the
        // identity handler remains active for the other tests.
        ru.abstractmenus.api.MenuExtension scratchOwner = new ru.abstractmenus.api.MenuExtension() {
            @Override public String name() { return "countingTestOwner"; }
            @Override public void onEnable(ru.abstractmenus.api.AbstractMenusApi api) {}
        };
        apiSupport.providers().placeholders().register("counting", new PlaceholderHandler() {
            @Override public String replacePlaceholder(Player p, String s) { return s; }
            @Override public String replace(Player p, String s) { callCount[0]++; return s; }
            @Override public List<String> replace(Player p, List<String> l) { return l; }
            @Override public void registerAll() {}
        }, 200, scratchOwner);
        try {
            ActionCommand action = buildAction("{ player = \"give %player_name% gold\" }");
            Player player = mock(Player.class);

            action.activate(player, null, null);

            org.junit.jupiter.api.Assertions.assertEquals(1, callCount[0],
                    "PlaceholderHandler.replace must be called exactly once per command");
        } finally {
            ((ru.abstractmenus.impl.ProviderRegistryImpl) apiSupport.providers()).unregisterAll(scratchOwner);
        }
    }

    @Test
    void activateNeverCallsChatApi() throws Exception {
        // Pins the Paper-deprecated chat() API out of the hot path.
        ActionCommand action = buildAction("{ player = \"heal\", ignorePlaceholder = true }");
        Player player = mock(Player.class);

        action.activate(player, null, null);

        verify(player, never()).chat(anyString());
    }

    private static ActionCommand buildAction(String hocon) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("val = " + hocon).getBytes(StandardCharsets.UTF_8));
        ConfigNode root = ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", in))
                .build()
                .load();
        return new ActionCommand.Serializer().deserialize(ActionCommand.class, root.node("val"));
    }
}
