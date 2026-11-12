package ru.abstractmenus.audit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.MockedStatic;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.data.actions.*;
import ru.abstractmenus.data.rules.RuleInventoryItem;
import ru.abstractmenus.datatype.*;
import ru.abstractmenus.hocon.api.*;
import ru.abstractmenus.hocon.api.serialize.*;
import ru.abstractmenus.hocon.api.source.ConfigSources;
import ru.abstractmenus.nms.title.Title;
import ru.abstractmenus.util.bukkit.BukkitTasks;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Constructor;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class TestActionsRulesRegressions {
    MockedStatic<AbstractMenusApi> apiStatic;
    NodeSerializers serializers;
    @BeforeEach void setup() {
        MockBukkit.mock().addSimpleWorld("world");
        ru.abstractmenus.api.text.Colors.init(false);
        ru.abstractmenus.util.MiniMessageUtil.init(false);
        AbstractMenusApi api = mock(AbstractMenusApi.class, RETURNS_DEEP_STUBS);
        apiStatic = mockStatic(AbstractMenusApi.class);
        apiStatic.when(AbstractMenusApi::get).thenReturn(api);
        when(api.providers().placeholders().resolve().replace(any(), anyString())).thenAnswer(i -> i.getArgument(1));
        when(api.providers().placeholders().resolve().replace(any(), anyList())).thenAnswer(i -> i.getArgument(1));
        ru.abstractmenus.api.Logger.set(java.util.logging.Logger.getLogger("audit"));
        serializers = NodeSerializers.defaults();
        serializers.register(TypeInt.class,new TypeInt.Serializer());
        serializers.register(TypeFloat.class,new TypeFloat.Serializer());
        serializers.register(TypeBool.class,new TypeBool.Serializer());
        serializers.register(TypeLocation.class,new TypeLocation.Serializer());
        BukkitTasks.setPlugin(null); BukkitTasks.setFoliaLib(null);
    }
    @AfterEach void cleanup() { apiStatic.close(); MockBukkit.unmock(); }
    ConfigNode node(String input) throws Exception {
        return ConfigurationLoader.builder().serializers(serializers)
            .source(ConfigSources.inputStream("audit",new ByteArrayInputStream(("val = "+input).getBytes(StandardCharsets.UTF_8))))
            .build().load().node("val");
    }
    @Test void soundAcceptsExplicitLocation() throws Exception {
        assertDoesNotThrow(() -> new ActionSound.Serializer().deserialize(ActionSound.class,
            node("{ name = \"entity.player.levelup\", location = \"world,0,64,0\" }")));
    }
    @Test void soundAcceptsPlaceholder() throws Exception {
        assertDoesNotThrow(() -> new ActionSound.Serializer().deserialize(ActionSound.class,
            node("\"%varp_sound%\"")));
    }
    @Test void customSoundUsesCategory() throws Exception {
        assertDoesNotThrow(() -> new ActionSoundCustom.Serializer()
            .deserialize(ActionSoundCustom.class,node("{ name = \"custom.bell\", category = \"MASTER\" }")));
    }
    @Test void titlePreservesTimingOrder() throws Exception {
        Title.init();
        ActionMessage action = new ActionMessage.Serializer().deserialize(ActionMessage.class,
            node("{ title = \"Hello\", fadeIn = 5, stay = 100, fadeOut = 20 }"));
        Player p = mock(Player.class);
        action.activate(p,null,null);
        verify(p).sendTitle("Hello","",5,100,20);
    }
    @Test void brokenInventoryItemFailsClosed() throws Exception {
        var item = new ru.abstractmenus.menu.item.SimpleItem();
        item.addProperty("count",new ru.abstractmenus.data.properties.PropCount.Serializer().deserialize(
            ru.abstractmenus.data.properties.PropCount.class,node("\"%varp_cost%\"")));
        Constructor<RuleInventoryItem> c=RuleInventoryItem.class.getDeclaredConstructor(List.class); c.setAccessible(true);
        RuleInventoryItem rule=c.newInstance(List.of(item));
        assertFalse(rule.check(mock(Player.class),null,null));
    }
    @Test void removesRemainderAcrossEquipment() throws Exception {
        Player p=MockBukkit.getMock().addPlayer();
        p.getInventory().setHelmet(new ItemStack(org.bukkit.Material.DIAMOND_HELMET));
        p.getInventory().setItemInOffHand(new ItemStack(org.bukkit.Material.DIAMOND_HELMET));
        Item item=mock(Item.class);
        when(item.build(any(),any())).thenReturn(new ItemStack(org.bukkit.Material.DIAMOND_HELMET,2));
        Constructor<ActionItemRemove> c=ActionItemRemove.class.getDeclaredConstructor();c.setAccessible(true);
        ActionItemRemove action=c.newInstance();
        var f=ActionItemRemove.class.getDeclaredField("items");f.setAccessible(true);f.set(action,List.of(item));
        action.activate(p,null,null);
        assertNull(p.getInventory().getHelmet());
        assertTrue(p.getInventory().getItemInOffHand().getType().isAir());
    }
    @Test void takeXpUsesCurrentLevelAndProgress() {
        Player p=mock(Player.class);
        when(p.calculateTotalExperiencePoints()).thenReturn(160);
        var handler=new ru.abstractmenus.handlers.LevelDefaultHandler();
        assertEquals(160,handler.getXp(p));
        handler.takeXp(p,1);
        verify(p).setExperienceLevelAndProgress(159);
        verify(p,never()).setLevel(anyInt());
        verify(p,never()).giveExp(anyInt());
    }
    @Test void chanceZeroNeverPasses() throws Exception {
        var rule=new ru.abstractmenus.data.rules.RuleChance.Serializer().deserialize(
            ru.abstractmenus.data.rules.RuleChance.class,node("0"));
        try (var random=mockStatic(java.util.concurrent.ThreadLocalRandom.class)) {
            var r=mock(java.util.concurrent.ThreadLocalRandom.class);
            random.when(java.util.concurrent.ThreadLocalRandom::current).thenReturn(r);
            when(r.nextInt(100)).thenReturn(0);
            assertFalse(rule.check(null,null,null));
        }
    }
    @Test void explicitRefreshSlotWorksWithoutClick() throws Exception {
        serializers.register(TypeSlot.class,new TypeSlot.Serializer());
        ActionItemRefresh action=new ActionItemRefresh.Serializer().deserialize(ActionItemRefresh.class,
            node("{ slot = 0, delay = 1 }"));
        Player player=mock(Player.class);
        Menu menu=mock(Menu.class);
        try (var tasks=mockStatic(BukkitTasks.class)) {
            tasks.when(() -> BukkitTasks.runForEntityLater(eq(player),any(Runnable.class),eq(1L)))
                .thenAnswer(i -> { ((Runnable)i.getArgument(1)).run(); return null; });
            action.activate(player,menu,null);
            verify(menu).refreshItem(any(),eq(player));
        }
    }
}
