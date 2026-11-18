package ru.abstractmenus.variables;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import ru.abstractmenus.MainConfig;
import ru.abstractmenus.util.bukkit.BukkitTasks;
import ru.abstractmenus.util.bukkit.TaskHandle;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TestVariablePersistence {
    @TempDir Path folder;
    private VariableManagerImpl manager;

    @BeforeEach
    void setUp() {
        ru.abstractmenus.api.Logger.set(java.util.logging.Logger.getLogger("variables-test"));
        var config = mock(MainConfig.class);
        when(config.isUseVariables()).thenReturn(true);
        when(config.getDbFolder()).thenReturn(folder);
        try (var tasks = mockStatic(BukkitTasks.class)) {
            tasks.when(() -> BukkitTasks.runTaskTimerAsync(any(),anyLong(),anyLong())).thenReturn(TaskHandle.NOOP);
            manager = new VariableManagerImpl(config);
        }
    }

    @AfterEach
    void tearDown() { manager.shutdown(); }

    private String diskValue(String name) throws Exception {
        try (var db = DriverManager.getConnection("jdbc:sqlite:" + folder.resolve("variables.db"));
             var statement = db.prepareStatement("SELECT value FROM variables WHERE key=?")) {
            statement.setString(1,"#global:" + name);
            try (var result = statement.executeQuery()) { return result.next() ? result.getString(1) : null; }
        }
    }

    @Test
    void writesPersistInSubmissionOrder() throws Exception {
        for (int i=0; i<100; i++) manager.saveGlobal(new VarImpl("score",String.valueOf(i),0),true);
        manager.deleteGlobal("score");
        manager.saveGlobal(new VarImpl("score","last",0),true);
        manager.shutdown();
        assertEquals("last",diskValue("score"));
    }

    @Test
    void savesReturnBeforeIoAndShutdownDrainsAcceptedWrites() throws Exception {
        var daoField = VariableManagerImpl.class.getDeclaredField("dao");
        daoField.setAccessible(true);
        var writerField = VariablesDao.class.getDeclaredField("writer");
        writerField.setAccessible(true);
        var writer = (ExecutorService)writerField.get(daoField.get(manager));
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        writer.execute(() -> {
            entered.countDown();
            try { release.await(5,TimeUnit.SECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        assertTrue(entered.await(1,TimeUnit.SECONDS));
        try {
            assertTimeout(java.time.Duration.ofSeconds(1),
                    () -> manager.saveGlobal(new VarImpl("reward","saved",0),true));
            var closing = CompletableFuture.runAsync(manager::shutdown);
            assertThrows(TimeoutException.class,() -> closing.get(50,TimeUnit.MILLISECONDS));
            release.countDown();
            closing.get(2,TimeUnit.SECONDS);
            assertEquals("saved",diskValue("reward"));
            manager.saveGlobal(new VarImpl("reward","too late",0),true);
            assertEquals("saved",manager.getGlobal("reward").value());
            assertEquals("saved",diskValue("reward"));
        } finally { release.countDown(); }
    }

    @Test
    void numericChangesPreserveGlobalAndPersonalExpiry() {
        long expiry = System.currentTimeMillis()+600000;
        manager.saveGlobal(new VarImpl("attempts","1",expiry),true);
        manager.savePersonal("Alice",new VarImpl("attempts","1",expiry),true);
        manager.modifyNumericGlobal("attempts",value -> value+1);
        manager.modifyNumericPersonal("Alice","attempts",value -> value+1);
        assertEquals(expiry,manager.getGlobal("attempts").expiry());
        assertEquals(expiry,manager.getPersonal("Alice","attempts").expiry());
        assertEquals(2,manager.getGlobal("attempts").intValue());
    }
}
