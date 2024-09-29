package ru.abstractmenus.data.actions;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Slot;
import ru.abstractmenus.menu.item.InventoryItem;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Logger;

import java.util.List;

public class ActionItemAdd implements Action {

    private final List<Item> items;

    private ActionItemAdd(List<Item> items){
        this.items = items;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        for(Item item : items){
            try {
                if(item instanceof InventoryItem) {
                    Slot slot = ((InventoryItem)item).getSlot(player, menu);
                    ItemStack built = item.build(player, menu);
                    slot.getSlots((s)->player.getInventory().setItem(s, built));
                    continue;
                }

                player.getInventory().addItem(item.build(player, menu));
            } catch (Exception e){
                Logger.severe("Cannot add item in player inventory: " + e.getMessage());
            }
        }
    }

    public static class Serializer implements NodeSerializer<ActionItemAdd> {

        @Override
        public ActionItemAdd deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionItemAdd(node.getList(Item.class));
        }

    }
}
