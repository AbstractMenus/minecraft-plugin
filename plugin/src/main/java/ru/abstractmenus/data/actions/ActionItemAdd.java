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
import java.util.Map;

public class ActionItemAdd implements Action {

    private final List<Item> items;

    private ActionItemAdd(List<Item> items) {
        this.items = items;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        for (Item item : items) {
            try {
                ItemStack built = item.build(player, menu);

                Map<Integer, ItemStack> notAdded = player.getInventory().addItem(built);

                if (!notAdded.isEmpty()) {
                    for (ItemStack itemStack : notAdded.values()) {
                        player.getWorld().dropItem(player.getLocation(), itemStack);
                    }
                }

            } catch (Exception e) {
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
