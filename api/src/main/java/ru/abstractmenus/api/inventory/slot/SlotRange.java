package ru.abstractmenus.api.inventory.slot;

import ru.abstractmenus.api.inventory.Slot;

import java.util.function.Consumer;

/**
 * Ranged slot defined with min and max indexes
 * # Config example
 * slot: "0-9"
 * slot: "12-45"
 */
public class SlotRange implements Slot {

    private final int min;
    private final int max;

    public SlotRange(int min, int max) {
        this.min = Math.max(min, 0);
        this.max = max;
    }

    @Override
    public void getSlots(Consumer<Integer> indexCb) {
        for (int i = min; i <= max; i++) {
            indexCb.accept(i);
        }
    }

}
