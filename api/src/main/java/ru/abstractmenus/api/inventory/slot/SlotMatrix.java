package ru.abstractmenus.api.inventory.slot;

import ru.abstractmenus.api.inventory.Slot;

import java.util.function.Consumer;

/**
 * A {@link ru.abstractmenus.api.inventory.Slot} variant that selects inventory
 * cells via a 2D character mask, making complex border and pattern layouts
 * readable directly in HOCON.
 *
 * <p>Each string in the mask represents one inventory row. Any character other
 * than {@code '-'} marks a cell as <em>selected</em>; {@code '-'} leaves the
 * cell empty. The HOCON parser pre-resolves each selected character to its
 * zero-based absolute index and passes the resulting array to this constructor
 * &mdash; callers never deal with the raw strings at runtime.
 *
 * <p>Use {@code SlotMatrix} for irregular shapes such as borders, cross
 * patterns, or icon clusters. For simpler needs prefer {@link SlotIndex}
 * (single cell), {@link SlotPos} (column/row pair), or {@link SlotRange}
 * (contiguous strip).
 *
 * <h2>HOCON example</h2>
 *
 * <pre>{@code
 * # border frame in a 3-row menu (rows 0-2, 9 columns each)
 * slot: [
 *   "xxxxxxxxx",
 *   "x-------x",
 *   "xxxxxxxxx"
 * ]
 * }</pre>
 *
 * @see SlotIndex
 * @see SlotPos
 * @see SlotRange
 * @see ru.abstractmenus.api.inventory.Slot
 */
public class SlotMatrix implements Slot {

    private final Integer[] slots;

    /**
     * Creates a {@code SlotMatrix} from a pre-resolved array of absolute slot
     * indices.
     *
     * <p>The array is produced by the HOCON deserializer, which converts each
     * non-{@code '-'} character in the mask rows to its zero-based inventory
     * index. Direct construction is only needed in tests or programmatic
     * menu builders.
     *
     * @param slots ordered array of zero-based slot indices to select;
     *              must not be {@code null}
     */
    public SlotMatrix(Integer[] slots) {
        this.slots = slots;
    }

    /**
     * Passes every pre-resolved slot index to {@code indexCb} in the order
     * they appear in the original mask (left-to-right, top-to-bottom).
     *
     * @param indexCb consumer invoked once per selected cell;
     *                never {@code null}
     *
     * @implNote Iteration order mirrors the HOCON mask: cells are visited
     *           row by row, left to right. The number of invocations equals
     *           the number of non-{@code '-'} characters in the original mask.
     */
    @Override
    public void getSlots(Consumer<Integer> indexCb) {
        for (int slot : slots) {
            indexCb.accept(slot);
        }
    }
}
