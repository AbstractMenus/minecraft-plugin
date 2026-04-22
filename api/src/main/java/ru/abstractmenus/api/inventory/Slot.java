package ru.abstractmenus.api.inventory;

import java.util.function.Consumer;

/**
 * Inventory slot representation.
 * Implementors of this interface returns one or multiple slots.
 * For example, range slot type returns several slots between x-y inclusive
 */
public interface Slot {

    /**
     * Get one or multiple slot indexes
     * @param indexCb Callback for slot indexes
     */
    void getSlots(Consumer<Integer> indexCb);

}
