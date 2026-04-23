package ru.abstractmenus.api.inventory.slot;

import ru.abstractmenus.api.inventory.Slot;

import java.util.function.Consumer;

/**
 * A {@link ru.abstractmenus.api.inventory.Slot} variant that fills every cell
 * in a contiguous, inclusive range of absolute slot indices.
 *
 * <p>Use {@code SlotRange} to paint a strip of items across a row or a span of
 * cells without enumerating each index individually. For a single cell use
 * {@link SlotIndex}; for {@code (column, row)} addressing use {@link SlotPos};
 * for a 2D character-mask pattern use {@link SlotMatrix}.
 *
 * <p>The {@code min} bound is silently clamped to {@code 0} if a negative value
 * is supplied. The {@code max} bound is used as-is; callers should ensure it
 * does not exceed the owning menu's {@link ru.abstractmenus.api.inventory.Menu#getSize()}
 * minus&nbsp;1.
 *
 * <h2>HOCON examples</h2>
 *
 * <pre>{@code
 * # fill the first row (slots 0-8)
 * slot: "0-8"
 *
 * # border band across slots 10-16
 * slot: "10-16"
 * }</pre>
 *
 * @see SlotIndex
 * @see SlotPos
 * @see SlotMatrix
 * @see ru.abstractmenus.api.inventory.Slot
 */
public class SlotRange implements Slot {

    private final int min;
    private final int max;

    /**
     * Creates a {@code SlotRange} spanning [{@code min}, {@code max}] inclusive.
     *
     * @param min lower bound, zero-based; clamped to {@code 0} if negative
     * @param max upper bound, zero-based, inclusive; must be &ge; {@code min}
     *            after clamping
     */
    public SlotRange(int min, int max) {
        this.min = Math.max(min, 0);
        this.max = max;
    }

    /**
     * Iterates every index in the range [{@code min}, {@code max}] inclusive
     * and passes each to {@code indexCb}.
     *
     * @param indexCb consumer invoked once per slot in ascending order;
     *                never {@code null}
     *
     * @implNote The callback is invoked {@code max - min + 1} times in
     *           strictly ascending order, starting from the clamped
     *           {@code min} value.
     */
    @Override
    public void getSlots(Consumer<Integer> indexCb) {
        for (int i = min; i <= max; i++) {
            indexCb.accept(i);
        }
    }

}
