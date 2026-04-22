package ru.abstractmenus.api.inventory.slot;

import ru.abstractmenus.api.inventory.Slot;

import java.util.function.Consumer;

/**
 * Slot defined with x and y coordinates
 * # Config example
 * slot: "1, 3"
 * slot { x: 1, y: 3 }
 */
public class SlotPos implements Slot {

    private final int x;
    private final int y;

    public SlotPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void getSlots(Consumer<Integer> indexCb) {
        indexCb.accept((y-1) * 9 + (x-1));
    }
    
}
