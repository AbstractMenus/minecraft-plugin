package ru.abstractmenus.api.inventory;

import java.util.function.Consumer;

/**
 * An addressable set of one or more inventory slot indices inside a
 * {@link Menu}.
 *
 * <p>Every item in a HOCON menu is bound to exactly one {@code Slot}. A slot
 * may resolve to a single index ({@link ru.abstractmenus.api.inventory.slot.SlotIndex}),
 * to {@code (row, col)} coordinates
 * ({@link ru.abstractmenus.api.inventory.slot.SlotPos}), to a contiguous
 * inclusive range ({@link ru.abstractmenus.api.inventory.slot.SlotRange}), or
 * to every index matched by a 2D character mask
 * ({@link ru.abstractmenus.api.inventory.slot.SlotMatrix}). The {@link Slot}
 * interface itself is the common view &mdash; callers iterate resolved
 * indices via {@link #getSlots(Consumer)} without caring which concrete
 * variant produced them.
 *
 * <h2>Example &mdash; iterating every slot an item occupies</h2>
 *
 * <pre>{@code
 * Slot slot = item.getSlot();
 * slot.getSlots(i -> inventory.setItem(i, stack));
 * }</pre>
 *
 * <h2>Menu usage</h2>
 *
 * The {@code slot} key inside an item block accepts any of the variants, and
 * the HOCON parser picks the matching {@code Slot} implementation:
 *
 * <pre>{@code
 * items: [
 *   { slot: 13,            material: DIAMOND }          # SlotIndex
 *   { slot: "2,4",         material: EMERALD }          # SlotPos
 *   { slot: "10-16",       material: GLASS_PANE }       # SlotRange
 *   {
 *     slot {
 *       matrix: [
 *         "XXXXXXXXX",
 *         "X       X",
 *         "XXXXXXXXX"
 *       ]
 *       symbol: X
 *     }
 *     material: BLACK_STAINED_GLASS_PANE                # SlotMatrix
 *   }
 * ]
 * }</pre>
 *
 * @see ru.abstractmenus.api.inventory.slot.SlotIndex
 * @see ru.abstractmenus.api.inventory.slot.SlotPos
 * @see ru.abstractmenus.api.inventory.slot.SlotRange
 * @see ru.abstractmenus.api.inventory.slot.SlotMatrix
 * @see Item
 */
public interface Slot {

    /**
     * Feeds every raw inventory index this {@code Slot} resolves to into
     * {@code indexCb}, in an implementation-defined order.
     *
     * <p>Single-index variants invoke the callback once; ranges and matrices
     * invoke it once per matched cell. Indices are zero-based and bounded by
     * the owning menu's {@link Menu#getSize()}; callers are responsible for
     * clamping against the actual {@link org.bukkit.inventory.Inventory} size
     * if the menu was resized after parsing.
     *
     * @param indexCb consumer invoked once per resolved index; never
     *                {@code null}
     *
     * @implNote Implementations must not hold thread state between
     *           invocations &mdash; the callback may be invoked synchronously
     *           from multiple render passes on the main server thread.
     */
    void getSlots(Consumer<Integer> indexCb);

}
