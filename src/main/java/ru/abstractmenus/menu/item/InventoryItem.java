package ru.abstractmenus.menu.item;

import org.bukkit.entity.Player;
import ru.abstractmenus.datatype.TypeSlot;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Slot;
import ru.abstractmenus.api.inventory.slot.SlotIndex;

public class InventoryItem extends SimpleItem {

    private TypeSlot slot = new TypeSlot(new SlotIndex(-1));

    public Slot getSlot(Player player, Menu menu){
        return slot.getSlot(player, menu);
    }

    public void setSlot(TypeSlot slot){
        this.slot = slot;
    }

    @Override
    public InventoryItem clone(){
        return (InventoryItem) super.clone();
    }
}
