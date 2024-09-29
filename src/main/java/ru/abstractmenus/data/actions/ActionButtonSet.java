package ru.abstractmenus.data.actions;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Slot;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.menu.item.InventoryItem;

import java.util.List;

public class ActionButtonSet implements Action {

    private final List<Item> items;

    public ActionButtonSet(List<Item> items) {
        this.items = items;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        for (Item item : items) {
            if (item instanceof InventoryItem) {
                Slot slot = ((InventoryItem)item).getSlot(player, menu);
                menu.setItem(slot, item, player);
            }
        }
    }

    public static class Serializer implements NodeSerializer<ActionButtonSet> {
        @Override
        public ActionButtonSet deserialize(Class<ActionButtonSet> type, ConfigNode node) throws NodeSerializeException {
            return new ActionButtonSet(node.getList(Item.class));
        }
    }

}
