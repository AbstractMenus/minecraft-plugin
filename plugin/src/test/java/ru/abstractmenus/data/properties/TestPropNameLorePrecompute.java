package ru.abstractmenus.data.properties;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.handler.PlaceholderHandler;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.source.ConfigSources;
import ru.abstractmenus.util.MiniMessageUtil;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pins the pre-compute optimisation in {@link PropName} and {@link PropLore}.
 *
 * <p>Static inputs (no {@code %placeholder%} / {@code ${var}}) must skip the
 * per-apply placeholder replace + MiniMessage round trip and reuse a cached
 * formatted string/list. Dynamic inputs must continue to call the placeholder
 * handler on every apply.
 *
 * <p>{@link MiniMessageUtil#init(boolean) init(false)} keeps the helper in
 * pass-through mode so the test classpath does not trigger Adventure's
 * {@code LegacyComponentSerializer} static init (which collides between the
 * Paper-bundled and standalone Adventure jars).
 */
class TestPropNameLorePrecompute {

    private static PlaceholderHandler previousHandler;
    private static int replaceCallCount;

    @BeforeAll
    static void setUp() {
        MiniMessageUtil.init(false);
        Colors.init(false);  // pass-through; avoids LegacyComponentSerializer init.
        previousHandler = Handlers.getPlaceholderHandler();
        Handlers.setPlaceholderHandler(new PlaceholderHandler() {
            @Override public String replacePlaceholder(Player p, String s) { return s; }
            @Override public String replace(Player p, String s) { replaceCallCount++; return s; }
            @Override public List<String> replace(Player p, List<String> l) { replaceCallCount++; return l; }
            @Override public void registerAll() {}
        });
    }

    @AfterAll
    static void tearDown() {
        Handlers.setPlaceholderHandler(previousHandler);
        // Intentionally leave MiniMessageUtil in inactive mode: re-init(true)
        // would trigger LegacyComponentSerializer.builder(), which collides
        // on the test classpath (two Adventure Provider impls). No other test
        // depends on active mode.
    }

    // ----- PropName -----

    @Test
    void propNameStaticInputSkipsHandlerCall() throws Exception {
        replaceCallCount = 0;
        PropName prop = buildName("\"Hello world\"");
        ItemMeta meta = mock(ItemMeta.class);

        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);
        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);
        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);

        assertEquals(0, replaceCallCount,
                "Static name must not invoke PlaceholderHandler.replace");
        verify(meta, times(3)).setDisplayName(anyString());
    }

    @Test
    void propNameDynamicInputInvokesHandlerEveryApply() throws Exception {
        replaceCallCount = 0;
        PropName prop = buildName("\"Hello %player_name%\"");
        ItemMeta meta = mock(ItemMeta.class);

        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);
        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);
        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);

        assertEquals(3, replaceCallCount,
                "Dynamic name must call PlaceholderHandler.replace once per apply");
    }

    @Test
    void propNameSetsDisplayNameContentMatchesInput() throws Exception {
        PropName prop = buildName("\"Static title\"");
        ItemMeta meta = mock(ItemMeta.class);

        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);

        ArgumentCaptor<String> captured = ArgumentCaptor.forClass(String.class);
        verify(meta).setDisplayName(captured.capture());
        // InactiveReplacer is pass-through; the Colors.of step still runs at
        // deserialization but doesn't change a string with no '&' codes.
        assertEquals("Static title", captured.getValue());
    }

    @Test
    void propNameDollarBracePlaceholderTreatedAsDynamic() throws Exception {
        replaceCallCount = 0;
        PropName prop = buildName("\"Money ${var_balance}\"");
        ItemMeta meta = mock(ItemMeta.class);

        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);

        assertEquals(1, replaceCallCount,
                "${...} placeholders must also disable pre-compute");
    }

    // ----- PropLore -----

    @Test
    void propLoreFullyStaticListSkipsHandlerCall() throws Exception {
        replaceCallCount = 0;
        PropLore prop = buildLore("[\"Line one\", \"Line two\", \"Line three\"]");
        ItemMeta meta = mock(ItemMeta.class);

        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);
        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);

        assertEquals(0, replaceCallCount,
                "Fully-static lore must not invoke PlaceholderHandler.replace");
        verify(meta, times(2)).setLore(anyList());
    }

    @Test
    void propLoreReusesSameCachedListReferenceAcrossCalls() throws Exception {
        PropLore prop = buildLore("[\"Static line\"]");
        ItemMeta meta = mock(ItemMeta.class);

        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);
        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);

        // Both invocations must hand setLore the same immutable List instance.
        ArgumentCaptor<List> captured = ArgumentCaptor.forClass(List.class);
        verify(meta, times(2)).setLore(captured.capture());
        assertSame(captured.getAllValues().get(0), captured.getAllValues().get(1),
                "Pre-computed lore should be a single shared immutable list");
    }

    @Test
    void propLoreAnyDynamicLineDisablesPreCompute() throws Exception {
        replaceCallCount = 0;
        // Mix: one static + one dynamic line — entire lore must go dynamic.
        PropLore prop = buildLore("[\"Static\", \"Hi %player_name%\"]");
        ItemMeta meta = mock(ItemMeta.class);

        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);
        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);

        assertEquals(2, replaceCallCount,
                "A single dynamic line must disable pre-compute for the whole lore");
    }

    @Test
    void propLoreEmptyListIsTreatedAsDynamic() throws Exception {
        // Edge case: empty list → canPrecompute returns false → dynamic path,
        // which still produces an empty list result.
        replaceCallCount = 0;
        PropLore prop = buildLore("[]");
        ItemMeta meta = mock(ItemMeta.class);

        prop.apply(mock(ItemStack.class), meta, mock(Player.class), null);

        assertEquals(1, replaceCallCount);
    }

    // ----- helpers -----

    private static PropName buildName(String hocon) throws Exception {
        ConfigNode root = loadNode("val = " + hocon);
        return new PropName.Serializer().deserialize(PropName.class, root.node("val"));
    }

    private static PropLore buildLore(String hocon) throws Exception {
        ConfigNode root = loadNode("val = " + hocon);
        return new PropLore.Serializer().deserialize(PropLore.class, root.node("val"));
    }

    private static ConfigNode loadNode(String hocon) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(hocon.getBytes(StandardCharsets.UTF_8));
        return ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", in))
                .build()
                .load();
    }
}
