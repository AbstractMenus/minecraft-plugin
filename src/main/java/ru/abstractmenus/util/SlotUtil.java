package ru.abstractmenus.util;

import ru.abstractmenus.api.inventory.Slot;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class SlotUtil {

    public static Collection<Integer> collect(Slot slot) {
        if (slot == null) return Collections.emptyList();
        Collection<Integer> list = new HashSet<>();
        slot.getSlots(list::add);
        return list;
    }

    public static boolean contains(Slot slot, int index) {
        return collect(slot).contains(index);
    }

}
