package ru.abstractmenus.data.rules;

import ru.abstractmenus.api.inventory.Slot;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.menu.item.InventoryItem;
import ru.abstractmenus.util.SlotUtil;
import ru.abstractmenus.util.bukkit.ItemUtil;

import java.util.List;

public class RuleInventoryItem implements Rule {

    private final List<Item> items;

    private RuleInventoryItem(List<Item> items){
        this.items = items;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        for(Item item : items) {
            if (item instanceof InventoryItem) {
                try {
                    ItemStack built = item.build(player, menu);
                    Slot slot = ((InventoryItem)item).getSlot(player, menu);

                    for (int index : SlotUtil.collect(slot)) {
                        ItemStack inventoryItem = player.getInventory().getItem(index);

                        if (!ItemUtil.isSimilar(built, inventoryItem)) return false;
                    }
                } catch (Exception e){
                    Logger.severe("Cannot check item in player inventory: " + e.getMessage());
                }
            } else {
                try {
                    ItemStack built = item.build(player, menu);
                    if(!player.getInventory().containsAtLeast(built, built.getAmount())) return false;
                } catch (Exception e){
                    Logger.severe("Cannot check item in player inventory: " + e.getMessage());
                }
            }
        }

        return true;
    }

    public static class Serializer implements NodeSerializer<RuleInventoryItem> {

        @Override
        public RuleInventoryItem deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleInventoryItem(node.getList(Item.class));
        }

    }
}
