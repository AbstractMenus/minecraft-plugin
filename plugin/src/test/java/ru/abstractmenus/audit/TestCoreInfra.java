package ru.abstractmenus.audit;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.ServicePriority;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockbukkit.mockbukkit.MockBukkit;
import ru.abstractmenus.MainConfig;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.variables.Var;
import ru.abstractmenus.command.Command;
import ru.abstractmenus.command.CommandManager;
import ru.abstractmenus.command.args.Argument;
import ru.abstractmenus.commands.varp.VarpSet;
import ru.abstractmenus.handlers.SkinsRestorerHandler;
import ru.abstractmenus.handlers.placeholder.PlaceholderDefaultHandler;
import ru.abstractmenus.util.TimeUtil;
import ru.abstractmenus.variables.VariableManagerImpl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TestCoreInfra {
    private org.mockbukkit.mockbukkit.ServerMock server;
    private org.bukkit.plugin.Plugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin("AbstractMenus");
        ru.abstractmenus.api.text.Colors.init(false);
        ru.abstractmenus.api.Logger.set(plugin.getLogger());
        var config = mock(MainConfig.class);
        when(config.getTimeDay()).thenReturn("d");
        when(config.getTimeHour()).thenReturn("h");
        when(config.getTimeMinute()).thenReturn("m");
        when(config.getTimeSecond()).thenReturn("s");
        TimeUtil.init(config);
    }

    @AfterEach
    void tearDown() { MockBukkit.unmock(); }

    @Test
    void adjacentPlaceholdersResolveIndependently() {
        var handler = new PlaceholderDefaultHandler();
        handler.registerAll();
        var player = server.addPlayer("Alice"); player.setLevel(7);
        assertEquals("Alice/7",handler.replace(player,"%player_name%/%player_level%"));
    }

    @Test
    void varpSetUsesTimeAndReplaceArgumentsAndHandlesMissingValue() {
        var manager = mock(VariableManagerImpl.class);
        when(manager.createBuilder()).thenAnswer(call -> new ru.abstractmenus.variables.VarImpl("","",0).toBuilder());
        var sender = mock(CommandSender.class);
        try (var singleton = mockStatic(VariableManagerImpl.class)) {
            singleton.when(VariableManagerImpl::instance).thenReturn(manager);
            var command = new VarpSet();
            long before = System.currentTimeMillis();
            command.execute(sender,new String[]{"Alice","buff","on","5m","false"});
            var variable = ArgumentCaptor.forClass(Var.class);
            verify(manager).savePersonal(eq("Alice"),variable.capture(),eq(false));
            assertEquals("on",variable.getValue().value());
            assertTrue(variable.getValue().expiry() >= before + 300000);
            assertTrue(variable.getValue().expiry() <= System.currentTimeMillis()+300000);
            clearInvocations(manager,sender);
            assertDoesNotThrow(() -> command.execute(sender,new String[]{"Alice","buff"}));
            verify(sender).sendMessage(any(String[].class));
            verifyNoInteractions(manager);
        }
    }

    @Test
    void namespacedCommandsExecuteAndCompleteThroughWrapper() {
        var manager = new CommandManager(plugin);
        var argument = mock(Argument.class);
        when(argument.suggest(any())).thenReturn(List.of("stone","sand"));
        var command = new Command("shop",List.of("store"),List.of(argument),"%s","%s",false);
        var calls = new AtomicInteger();
        command.setHandler((sender,context) -> calls.incrementAndGet());
        manager.register(command);
        var wrapper = server.getCommandMap().getCommand("abstractmenus:shop");
        wrapper.execute(server.getConsoleSender(),"abstractmenus:shop",new String[0]);
        wrapper.execute(server.getConsoleSender(),"shop",new String[0]);
        assertEquals(2,calls.get());
        assertEquals(List.of("stone","sand"),wrapper.tabComplete(server.getConsoleSender(),"abstractmenus:shop",new String[]{"s"}));
        assertSame(command,manager.get("abstractmenus:store"));
    }

    @Test
    void proxySkinModeRegistersOutgoingChannelIndependently() {
        assertFalse(server.getMessenger().isOutgoingChannelRegistered(plugin,"abstractmenus:main"));
        new SkinsRestorerHandler(true,plugin);
        assertTrue(server.getMessenger().isOutgoingChannelRegistered(plugin,"abstractmenus:main"));
    }
}
