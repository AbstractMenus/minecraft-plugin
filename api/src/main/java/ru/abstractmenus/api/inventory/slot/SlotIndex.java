package ru.abstractmenus.api.inventory.slot;

import ru.abstractmenus.api.inventory.Slot;

import java.util.function.Consumer;

/**
 * A {@link ru.abstractmenus.api.inventory.Slot} variant that addresses a
 * single inventory cell by its absolute zero-based index (0&nbsp;&hellip;&nbsp;53).
 *
 * <p>Use {@code SlotIndex} when you know the exact cell number, or when you
 * want the engine to auto-place the item in the first available cell.
 * For human-readable {@code (column, row)} addressing prefer
 * {@link SlotPos}; for a contiguous block of cells use {@link SlotRange};
 * for a 2D character-mask pattern use {@link SlotMatrix}.
 *
 * <p>The special value {@code -1} instructs the renderer to insert the item
 * into the first free slot of the inventory rather than a fixed position.
 *
 * <h2>HOCON examples</h2>
 *
 * <pre>{@code
 * # absolute index
 * slot: 13
 *
 * # last slot of a 6-row inventory
 * slot: 53
 *
 * # auto-insert into first free cell
 * slot: -1
 * }</pre>
 *
 * @see SlotPos
 * @see SlotRange
 * @see SlotMatrix
 * @see ru.abstractmenus.api.inventory.Slot
 */
public class SlotIndex implements Slot {

    private final int slot;

    /**
     * Creates a {@code SlotIndex} for the given absolute inventory index.
     *
     * @param slot zero-based slot index (0&nbsp;&hellip;&nbsp;53), or {@code -1} to
     *             auto-insert into the first free cell
     */
    public SlotIndex(int slot) {
        this.slot = slot;
    }

    /**
     * Returns the raw slot index stored in this instance.
     *
     * @return the zero-based index, or {@code -1} for auto-insert mode
     */
    public int getIndex() {
        return slot;
    }

    /**
     * Invokes {@code indexCb} exactly once with the stored slot index.
     *
     * @param indexCb consumer to receive the resolved index; never {@code null}
     *
     * @implNote The callback is always invoked synchronously and exactly once,
     *           regardless of whether the index is {@code -1}.
     */
    @Override
    public void getSlots(Consumer<Integer> indexCb) {
        indexCb.accept(slot);
    }

    /**
     * Factory shortcut &mdash; equivalent to {@code new SlotIndex(index)}.
     *
     * @param index zero-based slot index or {@code -1} for auto-insert
     * @return a new {@code SlotIndex} wrapping {@code index}
     */
    public static SlotIndex of(int index) {
        return new SlotIndex(index);
    }
    
}
