package ru.abstractmenus.api.inventory.slot;

import ru.abstractmenus.api.inventory.Slot;

import java.util.function.Consumer;

/**
 * Slot defined by cells matrix
 * # Config example
 * slot: [
 *   "xxxxxxxxx",
 *   "x-------x",
 *   "x-------x",
 *   "xxxxxxxxx",
 * ]
 */
public class SlotMatrix implements Slot {

    private final Integer[] slots;

    public SlotMatrix(Integer[] slots) {
        this.slots = slots;
    }

    @Override
    public void getSlots(Consumer<Integer> indexCb) {
        for (int slot : slots) {
            indexCb.accept(slot);
        }
    }
}
