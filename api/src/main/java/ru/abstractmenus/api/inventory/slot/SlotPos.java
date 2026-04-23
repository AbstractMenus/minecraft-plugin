package ru.abstractmenus.api.inventory.slot;

import ru.abstractmenus.api.inventory.Slot;

import java.util.function.Consumer;

/**
 * A {@link ru.abstractmenus.api.inventory.Slot} variant that addresses a
 * single inventory cell via a human-readable {@code (column, row)} pair.
 *
 * <p>Both axes are <em>1-based</em>: column&nbsp;1 is the leftmost column and
 * row&nbsp;1 is the top row. The pair is converted to a zero-based index via
 * {@code (row - 1) * 9 + (col - 1)}, so {@code (1,&nbsp;1)} maps to
 * index&nbsp;0 and {@code (9,&nbsp;6)} maps to index&nbsp;53.
 *
 * <p>Prefer {@code SlotPos} over {@link SlotIndex} when the column/row
 * position is more readable than the raw number. For a contiguous block
 * of cells use {@link SlotRange}; for a 2D character-mask use
 * {@link SlotMatrix}.
 *
 * <h2>HOCON examples</h2>
 *
 * <pre>{@code
 * # inline "col,row" string -- column 5, row 2 (index 13)
 * slot: "5, 2"
 *
 * # object form
 * slot { x: 5, y: 2 }
 * }</pre>
 *
 * @see SlotIndex
 * @see SlotRange
 * @see SlotMatrix
 * @see ru.abstractmenus.api.inventory.Slot
 */
public class SlotPos implements Slot {

    private final int x;
    private final int y;

    /**
     * Creates a {@code SlotPos} from 1-based column and row coordinates.
     *
     * @param x column index, 1&nbsp;&hellip;&nbsp;9 (left to right)
     * @param y row index, 1&nbsp;&hellip;&nbsp;6 (top to bottom)
     */
    public SlotPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Converts the stored {@code (column, row)} pair to a zero-based absolute
     * index and passes it to {@code indexCb}.
     *
     * @param indexCb consumer to receive the resolved index; never {@code null}
     *
     * @implNote The conversion formula is {@code (y - 1) * 9 + (x - 1)},
     *           assuming a standard 9-column chest inventory layout.
     */
    @Override
    public void getSlots(Consumer<Integer> indexCb) {
        indexCb.accept((y-1) * 9 + (x-1));
    }

}
