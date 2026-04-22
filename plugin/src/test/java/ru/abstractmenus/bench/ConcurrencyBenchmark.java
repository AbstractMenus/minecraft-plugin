package ru.abstractmenus.bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark: HashMap vs ConcurrentHashMap for menu / profile storage.
 *
 * Context in the codebase:
 * - MenuManager.menus = HashMap (not thread-safe, read/write from different threads)
 * - ProfileStorage.profiles = ConcurrentHashMap (thread-safe, but without cleanup)
 * - MenuManager.openedMenus = ConcurrentHashMap (iterated every tick)
 *
 * Measures the cost of the different access strategies under workloads
 * typical for the plugin.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ConcurrencyBenchmark {

    private HashMap<UUID, String> hashMap;
    private ConcurrentHashMap<UUID, String> concurrentMap;
    private UUID[] keys;

    @Param({"10", "50", "100"})
    private int playerCount;

    @Setup
    public void setup() {
        hashMap = new HashMap<>();
        concurrentMap = new ConcurrentHashMap<>();
        keys = new UUID[playerCount];

        for (int i = 0; i < playerCount; i++) {
            UUID uuid = UUID.randomUUID();
            keys[i] = uuid;
            hashMap.put(uuid, "menu_" + i);
            concurrentMap.put(uuid, "menu_" + i);
        }
    }

    // === Iterate every entry (UpdateTask pattern) ===

    @Benchmark
    public void hashMap_iterate(Blackhole bh) {
        for (Map.Entry<UUID, String> entry : hashMap.entrySet()) {
            bh.consume(entry.getKey());
            bh.consume(entry.getValue());
        }
    }

    @Benchmark
    public void concurrentMap_iterate(Blackhole bh) {
        for (Map.Entry<UUID, String> entry : concurrentMap.entrySet()) {
            bh.consume(entry.getKey());
            bh.consume(entry.getValue());
        }
    }

    // === Key lookup (getMenu / getProfile pattern) ===

    @Benchmark
    public void hashMap_lookup(Blackhole bh) {
        bh.consume(hashMap.get(keys[0]));
    }

    @Benchmark
    public void concurrentMap_lookup(Blackhole bh) {
        bh.consume(concurrentMap.get(keys[0]));
    }

    // === Put + remove cycle (join/quit pattern) ===

    @Benchmark
    public void hashMap_putRemove(Blackhole bh) {
        UUID uuid = UUID.randomUUID();
        hashMap.put(uuid, "test");
        bh.consume(hashMap.remove(uuid));
    }

    @Benchmark
    public void concurrentMap_putRemove(Blackhole bh) {
        UUID uuid = UUID.randomUUID();
        concurrentMap.put(uuid, "test");
        bh.consume(concurrentMap.remove(uuid));
    }
}
