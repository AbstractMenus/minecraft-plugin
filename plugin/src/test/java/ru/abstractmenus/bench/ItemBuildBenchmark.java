package ru.abstractmenus.bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark: simulates the clone + build pattern of menu items.
 *
 * Context in SimpleMenu.placeItems():
 * 1. Item.clone() — deep-copies the LinkedHashMap of properties (EVERY item)
 * 2. Item.build() — `new ItemStack` + apply ALL properties
 * 3. applyProperties() — getItemMeta()/setItemMeta() clones meta twice per
 *    property
 *
 * For 54 slots × 10 properties × 20 TPS that's 21,600 meta clones per sec.
 *
 * This benchmark measures the cost of clone() + the map copy so the upside
 * of caching them is visible.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ItemBuildBenchmark {

    // Simulated item properties (LinkedHashMap as in SimpleItem)
    private LinkedHashMap<String, Object> materialProps;
    private LinkedHashMap<String, Object> simpleProps;
    private LinkedHashMap<String, Object> allProps;

    @Setup
    public void setup() {
        materialProps = new LinkedHashMap<>();
        materialProps.put("material", "DIAMOND_SWORD");

        simpleProps = new LinkedHashMap<>();
        simpleProps.put("name", "&6Super Sword %player_name%");
        simpleProps.put("lore", Arrays.asList("&7Line 1", "&7Line 2 %player_level%", "&7Line 3"));
        simpleProps.put("enchant", "DAMAGE_ALL:5");
        simpleProps.put("flags", Arrays.asList("HIDE_ENCHANTS", "HIDE_ATTRIBUTES"));
        simpleProps.put("amount", 1);
        simpleProps.put("model", 12345);
        simpleProps.put("unbreakable", true);
        simpleProps.put("color", "255,0,0");
        simpleProps.put("customTag", "some_value");

        allProps = new LinkedHashMap<>();
        allProps.putAll(materialProps);
        allProps.putAll(simpleProps);
    }

    // === Current pattern: deep clone every time ===

    @Benchmark
    public void clone_deepCopy(Blackhole bh) {
        // Simulates SimpleItem.clone() — allocates fresh LinkedHashMaps
        LinkedHashMap<String, Object> clonedAll = new LinkedHashMap<>(allProps);
        LinkedHashMap<String, Object> clonedMat = new LinkedHashMap<>(materialProps);
        LinkedHashMap<String, Object> clonedSimple = new LinkedHashMap<>(simpleProps);
        bh.consume(clonedAll);
        bh.consume(clonedMat);
        bh.consume(clonedSimple);
    }

    // === Optimized: no clone (read-only access) ===

    @Benchmark
    public void noClone_directAccess(Blackhole bh) {
        // When properties are immutable, cloning is unnecessary
        bh.consume(allProps);
        bh.consume(materialProps);
        bh.consume(simpleProps);
    }

    // === Full loop: 54 items with clone (current) ===

    @Benchmark
    public void fullMenu_withClone(Blackhole bh) {
        for (int i = 0; i < 54; i++) {
            LinkedHashMap<String, Object> clonedAll = new LinkedHashMap<>(allProps);
            LinkedHashMap<String, Object> clonedMat = new LinkedHashMap<>(materialProps);
            LinkedHashMap<String, Object> clonedSimple = new LinkedHashMap<>(simpleProps);
            // Simulates build: iterate over properties
            for (Map.Entry<String, Object> entry : clonedAll.entrySet()) {
                bh.consume(entry.getValue());
            }
        }
    }

    // === Full loop: 54 items without clone (optimized) ===

    @Benchmark
    public void fullMenu_noClone(Blackhole bh) {
        for (int i = 0; i < 54; i++) {
            // Direct access — no deep copy
            for (Map.Entry<String, Object> entry : allProps.entrySet()) {
                bh.consume(entry.getValue());
            }
        }
    }
}
