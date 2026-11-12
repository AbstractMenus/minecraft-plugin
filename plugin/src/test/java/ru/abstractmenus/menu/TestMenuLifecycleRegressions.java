package ru.abstractmenus.menu;

import com.tcoded.folialib.FoliaLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.*;
import org.mockbukkit.mockbukkit.inventory.InventoryMock;
import ru.abstractmenus.MainConfig;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.Catalog;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.api.inventory.*;
import ru.abstractmenus.api.inventory.slot.SlotIndex;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.data.Actions;
import ru.abstractmenus.datatype.TypeSlot;
import ru.abstractmenus.listeners.InventoryListener;
import ru.abstractmenus.menu.animated.*;
import ru.abstractmenus.menu.generated.*;
import ru.abstractmenus.menu.item.MenuItem;
import ru.abstractmenus.services.MenuManager;
import ru.abstractmenus.util.MiniMessageUtil;
import ru.abstractmenus.util.bukkit.BukkitTasks;
import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TestMenuLifecycleRegressions {
    ServerMock server;
    org.bukkit.plugin.Plugin plugin;
    Player player;
    MenuManager manager;

    @BeforeEach void setup() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        ru.abstractmenus.api.Logger.set(plugin.getLogger());
        var api = mock(AbstractMenusApi.class, RETURNS_DEEP_STUBS);
        when(api.providers().placeholders().resolve().replace(any(), anyString()))
            .thenAnswer(c -> c.<String>getArgument(1));
        server.getServicesManager().register(AbstractMenusApi.class, api, plugin, ServicePriority.Normal);
        player = server.addPlayer("Alice");
        Colors.init(false);
        MiniMessageUtil.init(false);
        MainConfig config = mock(MainConfig.class);
        when(config.getMenusFolder()).thenReturn(Path.of("/private/tmp/menu-audit-unused"));
        manager = new MenuManager(plugin, config);
        BukkitTasks.setPlugin(plugin);
        BukkitTasks.setFoliaLib(mock(FoliaLib.class));
        ru.abstractmenus.util.bukkit.Events.setPlugin(plugin);
        server.getPluginManager().registerEvents(new InventoryListener(), plugin);
    }
    @AfterEach void teardown() {
        try { MockBukkit.unmock(); }
        finally { BukkitTasks.setPlugin(null); BukkitTasks.setFoliaLib(null); ru.abstractmenus.util.bukkit.Events.setPlugin(null); }
    }
    private static Actions count(AtomicInteger counter) {
        var a = new Actions();
        a.add((p,m,i) -> counter.incrementAndGet());
        return a;
    }
    private static void set(Object obj, Class<?> owner, String field, Object value) throws Exception {
        Field f = owner.getDeclaredField(field); f.setAccessible(true); f.set(obj,value);
    }
    private GeneratedMenu generated(Catalog<?> catalog) {
        var m = new GeneratedMenu("Generated",9);
        var item = new MenuItem(); item.setClickCooldown(0);
        m.setMatrix(new Matrix(new LinkedHashMap<>(Map.of(0,item))));
        m.setCatalog(catalog);
        return m;
    }
    private static Catalog<String> catalog(java.util.function.Function<Player,List<String>> fn) {
        return new Catalog<>() {
            public Collection<String> snapshot(Player p, Menu m) { return fn.apply(p); }
            public ValueExtractor extractor() { return (o,k) -> o.toString(); }
        };
    }
    @Test void generatedContextsAreIsolated() {
        Player bob = server.addPlayer("Bob");
        GeneratedMenu template = generated(catalog(p -> List.of("Alice-target","Bob-target")));
        manager.openMenu(player,template);
        GeneratedMenu aliceMenu = (GeneratedMenu) manager.getOpenedMenu(player);
        assertEquals("Alice-target",aliceMenu.getCurrentObject());
        manager.openMenu(bob,template);
        ((GeneratedMenu) manager.getOpenedMenu(bob)).nextPage(bob,1);
        aliceMenu.click(0,player,ClickType.LEFT);
        assertEquals("Alice-target",aliceMenu.getCurrentObject());
    }
    @Test void switchingMenusClosesOldInstanceOnce() {
        var closed = new AtomicInteger();
        var a = new SimpleMenu("A",9); a.setCloseActions(count(closed));
        manager.openMenu(player,a);
        manager.openMenu(player,new SimpleMenu("B",9));
        server.getScheduler().performTicks(5);
        assertEquals(1,closed.get());
    }
    @Test void oldCloseDoesNotUnregisterNewMenu() {
        var closedB = new AtomicInteger();
        manager.openMenu(player,new SimpleMenu("A",9));
        player.closeInventory();
        var b = new SimpleMenu("B",9); b.setCloseActions(count(closedB));
        manager.openMenu(player,b);
        Menu visibleB = manager.getOpenedMenu(player);
        server.getScheduler().performTicks(5);
        assertSame(visibleB.getInventory(),player.getOpenInventory().getTopInventory());
        assertSame(visibleB,manager.getOpenedMenu(player));
        assertEquals(0,closedB.get());
    }
    @Test void emptyCatalogCanNavigateAndRepopulate() {
        var rows = new ArrayList<String>();
        var template = generated(catalog(p -> rows));
        manager.openMenu(player,template);
        var opened = (GeneratedMenu) manager.getOpenedMenu(player);
        assertEquals(0,opened.getPages());
        assertDoesNotThrow(() -> opened.nextPage(player,1));
        assertEquals(0,opened.getPage());
        rows.add("new entry");
        assertDoesNotThrow(() -> opened.refresh(player));
        assertEquals(1,opened.getPages());
        assertNotNull(opened.getItem(0));
    }
    @Test void catalogShrinkClampsPage() {
        var rows = new ArrayList<>(List.of("first","second","third"));
        manager.openMenu(player,generated(catalog(p -> rows)));
        var opened = (GeneratedMenu) manager.getOpenedMenu(player);
        opened.nextPage(player,2);
        assertEquals(2,opened.getPage());
        rows.remove(2); rows.remove(1);
        opened.refresh(player);
        assertEquals(1,opened.getPages());
        assertEquals(0,opened.getPage());
        assertNotNull(opened.getItem(0));
    }
    @Test void animationRefreshReplaysCompletion() throws Exception {
        var ended = new AtomicInteger();
        var ctor = Frame.class.getDeclaredConstructor(long.class,boolean.class);
        ctor.setAccessible(true);
        Frame frame = ctor.newInstance(0L,true);
        frame.setItems(List.of());
        var template = new AnimatedMenu("Animated",9);
        template.setFrames(List.of(frame));
        template.setAnimEndActions(count(ended));
        manager.openMenu(player,template);
        var opened = (AnimatedMenu) manager.getOpenedMenu(player);
        opened.update(player); opened.update(player);
        assertEquals(1,ended.get());
        opened.refresh(player);
        opened.update(player); opened.update(player);
        assertEquals(2,ended.get());
    }
    @Test void refreshPreservesDepositsOverConfiguredItems() {
        var template = new SimpleMenu("Deposit",9);
        template.setDraggableSlots(new TypeSlot(new SlotIndex(0)));
        manager.openMenu(player,template);
        var opened = (SimpleMenu) manager.getOpenedMenu(player);
        opened.placeItem(player,0,new ItemStack(Material.DIAMOND,4));
        opened.getInventory().setItem(0,new ItemStack(Material.DIAMOND,4));
        var configured = new MenuItem();
        configured.setSlot(new TypeSlot(new SlotIndex(0)));
        opened.setItems(List.of(configured));
        opened.refresh(player);
        assertEquals(new ItemStack(Material.DIAMOND,4),opened.getInventory().getItem(0));
        assertEquals(new ItemStack(Material.DIAMOND,4),opened.getPlacedItem(0));
        assertNull(opened.getItem(0));
    }

    @Test void animationFramesPreserveDeposits() throws Exception {
        var ctor = Frame.class.getDeclaredConstructor(long.class,boolean.class);
        ctor.setAccessible(true);
        var item = new MenuItem(); item.setSlot(new TypeSlot(new SlotIndex(0)));
        Frame frame = ctor.newInstance(0L,true); frame.setItems(List.of(item));
        var template = new AnimatedMenu("Deposit",9);
        template.setFrames(List.of(frame));
        template.setDraggableSlots(new TypeSlot(new SlotIndex(0)));
        manager.openMenu(player,template);
        var opened = (AnimatedMenu)manager.getOpenedMenu(player);
        opened.placeItem(player,0,new ItemStack(Material.DIAMOND,4));
        opened.getInventory().setItem(0,new ItemStack(Material.DIAMOND,4));
        opened.update(player);
        assertEquals(new ItemStack(Material.DIAMOND,4),opened.getInventory().getItem(0));
        assertNull(opened.getItem(0));
        opened.refresh(player);
        assertEquals(new ItemStack(Material.DIAMOND,4),opened.getInventory().getItem(0));
    }

    private void defaultPlaceholders(Map<String,String> values) throws Exception {
        var variables = mock(ru.abstractmenus.variables.VariableManagerImpl.class);
        when(variables.getGlobal(anyString())).thenAnswer(c -> {
            String value = values.get(c.getArgument(0));
            if (value == null) return null;
            var v = mock(ru.abstractmenus.api.variables.Var.class);
            when(v.value()).thenReturn(value);
            return v;
        });
        set(null,ru.abstractmenus.variables.VariableManagerImpl.class,"instance",variables);
        var handler = new ru.abstractmenus.handlers.placeholder.PlaceholderDefaultHandler();
        handler.registerAll();
        when(AbstractMenusApi.get().providers().placeholders().resolve()).thenReturn(handler);
    }
    @Test void hotReplacementUnregistersOldActivator() throws Exception {
        var old = new SimpleMenu("Old",9);
        var block = player.getWorld().getBlockAt(10,64,10);
        block.setType(Material.STONE);
        var ctor = ru.abstractmenus.data.activators.OpenClickBlock.class.getDeclaredConstructor(List.class);
        ctor.setAccessible(true);
        var activator = ctor.newInstance(List.of(new ru.abstractmenus.datatype.TypeLocation(block.getLocation())));
        old.setActivators(List.of(activator));
        manager.addMenu("shop",old);
        var locked = new SimpleMenu("New locked",9);
        locked.setOpenRules((p,m,i) -> false);
        manager.addMenu("shop",locked);
        doAnswer(c -> { manager.openMenu(c.getArgument(0),c.getArgument(1),c.getArgument(2),c.getArgument(3)); return null; })
            .when(AbstractMenusApi.get()).openMenu(any(),any(),any(),any());
        var event = new org.bukkit.event.player.PlayerInteractEvent(player,
            org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK,null,block,org.bukkit.block.BlockFace.UP,
            org.bukkit.inventory.EquipmentSlot.HAND);
        server.getPluginManager().callEvent(event);
        assertNull(manager.getOpenedMenu(player));
        assertSame(locked,manager.getMenu("shop"));
    }
    @Test void dynamicLowercaseDyeMatchesLiteral() throws Exception {
        defaultPlaceholders(Map.of("color","red"));
        org.bukkit.Color actual = new ru.abstractmenus.datatype.TypeColor("%var_get:color%").getColor(player,null);
        assertEquals(org.bukkit.DyeColor.RED.getColor(),actual);
    }
    @Test void generatedStaticPlaceholdersSeeCurrentSnapshot() throws Exception {
        defaultPlaceholders(Map.of());
        var template = generated(catalog(p -> List.of("one","two")));
        var label = new MenuItem(); label.setSlot(new TypeSlot(new SlotIndex(8)));
        var ctor = ru.abstractmenus.data.properties.PropName.class.getDeclaredConstructor(String.class);
        ctor.setAccessible(true);
        label.addProperty("name",ctor.newInstance("Entries: %ctg_elements%"));
        template.setItems(List.of(label));
        manager.openMenu(player,template);
        var opened = (GeneratedMenu)manager.getOpenedMenu(player);
        assertNotNull(opened.getItem(8));
        assertEquals("Entries: 2",opened.getInventory().getItem(8).getItemMeta().getDisplayName());
        assertEquals(2,opened.getSnapshot().size());
        opened.refresh(player);
        assertNotNull(opened.getItem(8));
    }
    @Test void typedInventoryAllowsBottomOnlyDrags() {
        var template = new SimpleMenu("Hopper",9); template.setType(InventoryType.HOPPER);
        template.setDraggableSlots(new TypeSlot(new SlotIndex(0)));
        manager.openMenu(player,template);
        var opened = manager.getOpenedMenu(player);
        assertEquals(9,opened.getSize()); assertEquals(5,opened.getInventory().getSize());
        var event = new org.bukkit.event.inventory.InventoryDragEvent(player.getOpenInventory(),
            new ItemStack(Material.DIAMOND,1),new ItemStack(Material.DIAMOND,2),false,
            Map.of(5,new ItemStack(Material.DIAMOND,1)));
        assertTrue(event.getRawSlots().stream().allMatch(i -> i >= opened.getInventory().getSize()));
        server.getPluginManager().callEvent(event);
        assertFalse(event.isCancelled());
    }
    @Test void cancelledTransfersNeverBecomeDeposits() {
        var template = new SimpleMenu("Deposit",9);
        template.setDraggableSlots(new TypeSlot(new SlotIndex(0)));
        manager.openMenu(player,template);
        var menu = (AbstractMenu)manager.getOpenedMenu(player);
        player.setItemOnCursor(new ItemStack(Material.DIAMOND,10));
        var listener = new InventoryListener();
        for (boolean cancelledEarly : new boolean[]{true,false}) {
            var event = new org.bukkit.event.inventory.InventoryClickEvent(player.getOpenInventory(),
                    org.bukkit.event.inventory.InventoryType.SlotType.CONTAINER,0,ClickType.LEFT,
                    org.bukkit.event.inventory.InventoryAction.PLACE_ALL);
            event.setCancelled(cancelledEarly);
            listener.onInventoryClick(event);
            listener.reconcileClick(event);
            event.setCancelled(true);
            server.getScheduler().performTicks(2);
            assertTrue(menu.getPlacedItem(0).getType().isAir());
        }
        var drag = new org.bukkit.event.inventory.InventoryDragEvent(player.getOpenInventory(),
                new ItemStack(Material.DIAMOND,9),new ItemStack(Material.DIAMOND,10),false,
                Map.of(0,new ItemStack(Material.DIAMOND)));
        drag.setCancelled(true);
        listener.onDrag(drag);
        listener.reconcileDrag(drag);
        listener.onInventoryClose(new org.bukkit.event.inventory.InventoryCloseEvent(player.getOpenInventory()));
        assertEquals(10,player.getItemOnCursor().getAmount());
        assertTrue(player.getWorld().getEntitiesByClass(org.bukkit.entity.Item.class).isEmpty());
    }

    @Test void acceptedTransfersTrackPhysicalAmountsAndCloseReturnsOnce() {
        var template = new SimpleMenu("Deposit",9);
        template.setDraggableSlots(new TypeSlot(new SlotIndex(0)));
        manager.openMenu(player,template);
        var menu = (AbstractMenu)manager.getOpenedMenu(player);
        var listener = new InventoryListener();
        player.setItemOnCursor(new ItemStack(Material.DIAMOND,10));
        var place = new org.bukkit.event.inventory.InventoryClickEvent(player.getOpenInventory(),
                org.bukkit.event.inventory.InventoryType.SlotType.CONTAINER,0,ClickType.LEFT,
                org.bukkit.event.inventory.InventoryAction.PLACE_SOME);
        listener.onInventoryClick(place);
        listener.reconcileClick(place);
        menu.getInventory().setItem(0,new ItemStack(Material.DIAMOND,6));
        player.setItemOnCursor(new ItemStack(Material.DIAMOND,4));
        server.getScheduler().performTicks(2);
        assertEquals(6,menu.getPlacedItem(0).getAmount());
        var take = new org.bukkit.event.inventory.InventoryClickEvent(player.getOpenInventory(),
                org.bukkit.event.inventory.InventoryType.SlotType.CONTAINER,0,ClickType.RIGHT,
                org.bukkit.event.inventory.InventoryAction.PICKUP_HALF);
        listener.onInventoryClick(take);
        listener.reconcileClick(take);
        menu.getInventory().setItem(0,new ItemStack(Material.DIAMOND,3));
        server.getScheduler().performTicks(2);
        assertEquals(3,menu.getPlacedItem(0).getAmount());
        player.closeInventory();
        menu.close(player,false);
        assertEquals(3,player.getWorld().getEntitiesByClass(org.bukkit.entity.Item.class).stream()
                .mapToInt(item -> item.getItemStack().getAmount()).sum());
    }
    @Test void unloadOnOwningThreadReturnsDepositsSynchronously() {
        var closed = new AtomicInteger();
        var template = new SimpleMenu("Deposit",9);
        template.setDraggableSlots(new TypeSlot(new SlotIndex(0)));
        template.setCloseActions(count(closed));
        manager.openMenu(player,template);
        var menu = (AbstractMenu)manager.getOpenedMenu(player);
        menu.placeItem(player,0,new ItemStack(Material.DIAMOND,4));
        menu.getInventory().setItem(0,new ItemStack(Material.DIAMOND,4));
        assertTrue(org.bukkit.Bukkit.isOwnedByCurrentRegion(player));
        manager.unloadAll();
        assertEquals(1,closed.get());
        assertNull(manager.getOpenedMenu(player));
        assertNotSame(menu.getInventory(),player.getOpenInventory().getTopInventory());
        assertEquals(4,player.getWorld().getEntitiesByClass(org.bukkit.entity.Item.class).stream()
                .mapToInt(item -> item.getItemStack().getAmount()).sum());
        server.getScheduler().performTicks(2);
        assertEquals(1,closed.get());
    }

    @Test void disablingPluginClosesWithoutScheduling() {
        var disabledPlugin = mock(org.bukkit.plugin.Plugin.class);
        var config = mock(MainConfig.class);
        when(config.getMenusFolder()).thenReturn(Path.of("/private/tmp/menu-audit-unused"));
        var disablingManager = new MenuManager(disabledPlugin,config);
        var menu = mock(Menu.class);
        disablingManager.setOpenedMenu(player,menu);
        try (var bukkit = mockStatic(org.bukkit.Bukkit.class,CALLS_REAL_METHODS)) {
            bukkit.when(() -> org.bukkit.Bukkit.isOwnedByCurrentRegion(player)).thenReturn(false);
            disablingManager.unloadAll();
            verify(menu).close(player,true);
        }
    }
}
