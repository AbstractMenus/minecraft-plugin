package ru.abstractmenus.audit;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.ServicePriority;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import ru.abstractmenus.MainConfig;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.data.EntityData;
import ru.abstractmenus.data.activators.OpenPlate;
import ru.abstractmenus.data.activators.OpenShiftClickEntity;
import ru.abstractmenus.datatype.TypeLocation;
import ru.abstractmenus.extractors.EntityExtractor;
import ru.abstractmenus.extractors.NPCExtractor;
import ru.abstractmenus.handlers.placeholder.PlaceholderDefaultHandler;
import ru.abstractmenus.placeholders.hooks.VarPlaceholders;
import ru.abstractmenus.variables.VarImpl;
import ru.abstractmenus.variables.VariableManagerImpl;
import ru.abstractmenus.util.TimeUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TestActivatorsPlaceholders {
    private AbstractMenusApi api;
    private Player player;
    private VariableManagerImpl variables;
    private PlaceholderDefaultHandler handler;

    @BeforeEach
    void setUp() {
        var server = MockBukkit.mock();
        var plugin = MockBukkit.createMockPlugin();
        player = server.addPlayer("Alice");
        api = mock(AbstractMenusApi.class, RETURNS_DEEP_STUBS);
        handler = new PlaceholderDefaultHandler();
        handler.registerAll();
        when(api.providers().placeholders().resolve()).thenReturn(handler);
        server.getServicesManager().register(AbstractMenusApi.class, api, plugin, ServicePriority.Normal);
        variables = new VariableManagerImpl(new MainConfig());
        var timeConfig = mock(MainConfig.class);
        when(timeConfig.getTimeDay()).thenReturn("d");
        when(timeConfig.getTimeHour()).thenReturn("h");
        when(timeConfig.getTimeMinute()).thenReturn("m");
        when(timeConfig.getTimeSecond()).thenReturn("s");
        TimeUtil.init(timeConfig);
    }

    @AfterEach
    void tearDown() {
        variables.shutdown();
        MockBukkit.unmock();
    }

    private <T> T construct(Class<T> type, List<?> entries) throws Exception {
        var constructor = type.getDeclaredConstructor(List.class);
        constructor.setAccessible(true);
        return constructor.newInstance(entries);
    }

    @Test
    void shiftClickSupportsOptionalName() throws Exception {
        Player sneaking = mock(Player.class);
        when(sneaking.isSneaking()).thenReturn(true);
        var cow = mock(LivingEntity.class);
        when(cow.getType()).thenReturn(EntityType.COW);
        when(cow.getName()).thenReturn("Shop");
        for (String name : new String[]{null, "Shop", "Other"}) {
            clearInvocations(api);
            var activator = construct(OpenShiftClickEntity.class, List.of(new EntityData(EntityType.COW, name)));
            var menu = mock(ru.abstractmenus.api.inventory.Menu.class);
            activator.setTargetMenu(menu);
            activator.onEntityClick(new PlayerInteractEntityEvent(sneaking, cow, EquipmentSlot.HAND));
            verify(api, times("Other".equals(name) ? 0 : 1)).openMenu(activator, cow, sneaking, menu);
        }
    }

    @Test
    void physicalPressurePlateDoesNotNeedHand() throws Exception {
        var plate = player.getWorld().getBlockAt(10,64,10);
        plate.setType(Material.STONE_PRESSURE_PLATE);
        var activator = construct(OpenPlate.class, List.of(new TypeLocation(plate.getLocation())));
        var menu = mock(ru.abstractmenus.api.inventory.Menu.class);
        activator.setTargetMenu(menu);
        activator.onPlateEnter(new PlayerInteractEvent(player, Action.PHYSICAL, null, plate, BlockFace.SELF, null));
        verify(api).openMenu(activator, plate, player, menu);
    }

    @Test
    void commonAndLivingEntityKeysRemainAvailable() {
        var cow = mock(LivingEntity.class);
        when(cow.getType()).thenReturn(EntityType.COW);
        when(cow.getName()).thenReturn("Shop");
        when(cow.getLastDamage()).thenReturn(2.0);
        assertEquals("COW", EntityExtractor.INSTANCE.extract(cow,"entity_type"));
        assertEquals("Shop", EntityExtractor.INSTANCE.extract(cow,"entity_name"));
        assertEquals("2.0", EntityExtractor.INSTANCE.extract(cow,"entity_last_damage"));
        assertEquals("Alice", EntityExtractor.INSTANCE.extract(player,"player_name"));
    }

    @Test
    void npcUsesCurrentEntityApi() {
        var npc = mock(NPC.class);
        var entity = mock(LivingEntity.class);
        when(entity.getType()).thenReturn(EntityType.COW);
        when(npc.getEntity()).thenReturn(entity);
        assertEquals("COW", NPCExtractor.INSTANCE.extract(npc,"npc_entity_type"));
        when(npc.getEntity()).thenReturn(null);
        assertEquals("", NPCExtractor.INSTANCE.extract(npc,"npc_entity_type"));
    }

    @Test
    void variableHooksSupportNamesDefaultsAndLegacyForms() {
        long expiry = System.currentTimeMillis() + 600000;
        variables.cache("#global:treasury",new VarImpl("treasury","42",expiry));
        variables.cache("alice:balance",new VarImpl("balance","17",expiry));
        assertEquals("42",handler.replace(player,"%var_treasury%"));
        assertEquals("42",handler.replace(player,"%var_:treasury%"));
        assertEquals("42",handler.replace(player,"%var_get:treasury%"));
        assertEquals("17",handler.replace(player,"%varp_balance:0%"));
        assertEquals("17",handler.replace(player,"%varp_:balance%"));
        assertEquals("fallback",handler.replace(player,"%var_missing:fallback%"));
        assertEquals("fallback",handler.replace(player,"%varp_missing:fallback%"));
        assertEquals("fallback",handler.replace(player,"%vart_missing:fallback%"));
        assertEquals("fallback",handler.replace(player,"%varpt_missing:fallback%"));
        assertEquals(new VarPlaceholders.VarTempHook().replace(":treasury",player),
                handler.replace(player,"%vart_treasury%"));
        assertEquals(new VarPlaceholders.VarTempPlayerHook().replace(":balance",player),
                handler.replace(player,"%varpt_balance%"));
    }
}
