package ru.abstractmenus.bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;

/**
 * Benchmark: SlotUtil.contains() (allocates a fresh HashSet every call)
 * vs direct iteration without allocating a collection.
 *
 * Context in the codebase: SlotUtil.contains(slot, index) runs on every
 * inventory click. Each call does `new HashSet<>()`, populates it via
 * getSlots(), then checks contains().
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class SlotContainsBenchmark {

    // Simulated Slot with a range of indices (typical 54-slot menu)
    private static final int[] SLOT_RANGE = new int[54];
    static {
        for (int i = 0; i < 54; i++) SLOT_RANGE[i] = i;
    }

    private int targetSlotHit;
    private int targetSlotMiss;

    @Setup
    public void setup() {
        targetSlotHit = 27;   // Middle of the range — hit
        targetSlotMiss = 99;  // Out of range — miss
    }

    // Simulates Slot.getSlots(IntConsumer)
    private void getSlots(IntConsumer consumer) {
        for (int slot : SLOT_RANGE) {
            consumer.accept(slot);
        }
    }

    // === Current implementation: fresh HashSet per call ===

    private boolean containsCurrent(int index) {
        Set<Integer> set = new HashSet<>();
        getSlots(set::add);  // Autoboxes 54 int → Integer
        return set.contains(index);
    }

    // === Optimized: direct iteration, zero allocation ===

    private boolean containsOptimized(int index) {
        for (int slot : SLOT_RANGE) {
            if (slot == index) return true;
        }
        return false;
    }

    // === Optimized with a boolean[] cache (for fixed slot ranges) ===

    private boolean[] cachedSlots;

    @Setup
    public void setupCache() {
        cachedSlots = new boolean[54];
        for (int slot : SLOT_RANGE) {
            if (slot < cachedSlots.length) cachedSlots[slot] = true;
        }
    }

    private boolean containsCached(int index) {
        return index >= 0 && index < cachedSlots.length && cachedSlots[index];
    }

    // ===== Benchmarks =====

    @Benchmark
    public void current_hit(Blackhole bh) {
        bh.consume(containsCurrent(targetSlotHit));
    }

    @Benchmark
    public void optimized_hit(Blackhole bh) {
        bh.consume(containsOptimized(targetSlotHit));
    }

    @Benchmark
    public void cached_hit(Blackhole bh) {
        bh.consume(containsCached(targetSlotHit));
    }

    @Benchmark
    public void current_miss(Blackhole bh) {
        bh.consume(containsCurrent(targetSlotMiss));
    }

    @Benchmark
    public void optimized_miss(Blackhole bh) {
        bh.consume(containsOptimized(targetSlotMiss));
    }

    @Benchmark
    public void cached_miss(Blackhole bh) {
        bh.consume(containsCached(targetSlotMiss));
    }

    // === Batch: 10 clicks in a row (rapid-click pattern) ===

    @Benchmark
    public void current_rapidClicks(Blackhole bh) {
        for (int i = 0; i < 10; i++) {
            bh.consume(containsCurrent(i * 5));
        }
    }

    @Benchmark
    public void cached_rapidClicks(Blackhole bh) {
        for (int i = 0; i < 10; i++) {
            bh.consume(containsCached(i * 5));
        }
    }
}
