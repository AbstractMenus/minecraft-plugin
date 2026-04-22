package ru.abstractmenus.api.inventory.slot;

import ru.abstractmenus.api.inventory.Slot;

import java.util.function.Consumer;

/**
 * Slot defined just by index
 * # Config example
 * slot: 0
 * slot: 51
 * slot: -1 (auto insert in first free slot)
 */
public class SlotIndex implements Slot {

    private final int slot;

    public SlotIndex(int slot) {
        this.slot = slot;
    }

    public int getIndex() {
        return slot;
    }

    @Override
    public void getSlots(Consumer<Integer> indexCb) {
        indexCb.accept(slot);
    }

    public static SlotIndex of(int index) {
        return new SlotIndex(index);
    }
    
}
