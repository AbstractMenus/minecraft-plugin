package ru.abstractmenus.util;

import org.junit.jupiter.api.Test;
import ru.abstractmenus.api.inventory.Slot;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class TestSlotUtil {

    private static Slot slotsOf(Integer... indices) {
        return consumer -> {
            for (Integer i : indices) consumer.accept(i);
        };
    }

    @Test
    void collectOnNullSlotReturnsEmpty() {
        Collection<Integer> result = SlotUtil.collect(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void collectGathersAllEmittedIndices() {
        Collection<Integer> result = SlotUtil.collect(slotsOf(1, 3, 5));
        assertEquals(3, result.size());
        assertTrue(result.containsAll(List.of(1, 3, 5)));
    }

    @Test
    void collectDeduplicatesRepeatedIndices() {
        Collection<Integer> result = SlotUtil.collect(slotsOf(1, 1, 2));
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(1, 2)));
    }

    @Test
    void collectEmptySlotReturnsEmptyCollection() {
        Collection<Integer> result = SlotUtil.collect(slotsOf());
        assertTrue(result.isEmpty());
    }

    @Test
    void containsFindsEmittedIndex() {
        assertTrue(SlotUtil.contains(slotsOf(4, 7, 10), 7));
    }

    @Test
    void containsReturnsFalseForMissingIndex() {
        assertFalse(SlotUtil.contains(slotsOf(4, 7, 10), 5));
    }

    @Test
    void containsReturnsFalseForNullSlot() {
        assertFalse(SlotUtil.contains(null, 0));
    }

    @Test
    void containsShortCircuitsLogically() {
        // The current impl walks the full emission and flips a flag;
        // this test just documents that it still returns the right answer
        // when the match is at the very end.
        assertTrue(SlotUtil.contains(slotsOf(0, 1, 2, 3, 4, 5, 6, 7, 8), 8));
    }

    @Test
    void collectRespectsSlotContract() {
        // Custom slot that uses an external counter to verify Consumer usage.
        int[] calls = {0};
        Slot slot = (Consumer<Integer> consumer) -> {
            calls[0]++;
            consumer.accept(42);
        };
        SlotUtil.collect(slot);
        assertEquals(1, calls[0]);
    }
}
