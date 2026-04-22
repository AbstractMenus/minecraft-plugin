package ru.abstractmenus.data.actions;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.handler.PlaceholderHandler;
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

    private static PlaceholderHandler previousHandler;

    @BeforeAll
    static void installIdentityHandler() {
        previousHandler = Handlers.getPlaceholderHandler();
        Handlers.setPlaceholderHandler(new PlaceholderHandler() {
            @Override public String replacePlaceholder(Player p, String s) { return s; }
            @Override public String replace(Player p, String s) { return s; }
            @Override public List<String> replace(Player p, List<String> l) { return l; }
            @Override public void registerAll() {}
        });
    }

    @AfterAll
    static void restore() {
        Handlers.setPlaceholderHandler(previousHandler);
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
        PlaceholderHandler saved = Handlers.getPlaceholderHandler();
        Handlers.setPlaceholderHandler(new PlaceholderHandler() {
            @Override public String replacePlaceholder(Player p, String s) { return s; }
            @Override public String replace(Player p, String s) { callCount[0]++; return s; }
            @Override public List<String> replace(Player p, List<String> l) { return l; }
            @Override public void registerAll() {}
        });
        try {
            ActionCommand action = buildAction("{ player = \"give %player_name% gold\" }");
            Player player = mock(Player.class);

            action.activate(player, null, null);

            org.junit.jupiter.api.Assertions.assertEquals(1, callCount[0],
                    "PlaceholderHandler.replace must be called exactly once per command");
        } finally {
            Handlers.setPlaceholderHandler(saved);
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
