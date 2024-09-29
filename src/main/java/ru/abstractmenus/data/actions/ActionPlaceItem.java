package ru.abstractmenus.data.actions;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Slot;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.menu.AbstractMenu;
import ru.abstractmenus.menu.item.InventoryItem;

import java.util.List;

public class ActionPlaceItem implements Action {

    private final List<Item> items;

    public ActionPlaceItem(List<Item> items) {
        this.items = items;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if (!(menu instanceof AbstractMenu)) return;

        AbstractMenu am = (AbstractMenu) menu;

        for (Item item : items) {
            if (item instanceof InventoryItem) {
                Slot slot = ((InventoryItem)item).getSlot(player, menu);
                ItemStack built = item.build(player, menu);
                am.placeItemQuiet(player, slot, built);
            }
        }
    }

    public static class Serializer implements NodeSerializer<ActionPlaceItem> {
        @Override
        public ActionPlaceItem deserialize(Class<ActionPlaceItem> type, ConfigNode node) throws NodeSerializeException {
            return new ActionPlaceItem(node.getList(Item.class));
        }
    }

}
