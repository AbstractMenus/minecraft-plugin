package ru.abstractmenus.data.rules;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Slot;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.menu.AbstractMenu;
import ru.abstractmenus.menu.item.InventoryItem;
import ru.abstractmenus.util.SlotUtil;
import ru.abstractmenus.util.bukkit.ItemUtil;

import java.util.LinkedList;
import java.util.List;

public class RulePlacedItem implements Rule {

    private final List<InventoryItem> items;

    private RulePlacedItem(List<InventoryItem> items) {
        this.items = items;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        if (!(menu instanceof AbstractMenu)) return false;

        AbstractMenu am = (AbstractMenu) menu;

        for (InventoryItem item : items) {
            Slot slot = item.getSlot(player, menu);
            ItemStack built = item.build(player, menu);

            for (int index : SlotUtil.collect(slot)) {
                ItemStack placed = am.getPlacedItem(index);

                if (!ItemUtil.isSimilar(built, placed)) return false;
            }
        }

        return true;
    }

    public static class Serializer implements NodeSerializer<RulePlacedItem> {

        @Override
        public RulePlacedItem deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            List<Item> items = node.getList(Item.class);
            List<InventoryItem> inventoryItems = new LinkedList<>();

            for (Item item : items) {
                if (item instanceof InventoryItem) {
                    inventoryItems.add((InventoryItem) item);
                } else {
                    throw new NodeSerializeException(node, "Invalid item format. Item must contain slot");
                }
            }

            return new RulePlacedItem(inventoryItems);
        }

    }
}
