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

public class ActionItemRemove implements Action {

    private List<Item> items;

    private void setItems(List<Item> items) {
        this.items = items;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        for(Item item : items) {
            try{
                if(item instanceof InventoryItem) {
                    Slot slot = ((InventoryItem)item).getSlot(player, menu);
                    slot.getSlots((s)->player.getInventory().clear(s));
                    continue;
                }

                ItemStack built = item.build(player, menu);

                if (!player.getInventory().removeItem(built).isEmpty()) {
                    if (built.isSimilar(player.getInventory().getHelmet())) {
                        decrement(built, player.getInventory().getHelmet(),
                                ()->player.getInventory().setHelmet(null));
                        continue;
                    }

                    if (built.isSimilar(player.getInventory().getChestplate())) {
                        decrement(built, player.getInventory().getChestplate(),
                                ()->player.getInventory().setChestplate(null));
                        continue;
                    }

                    if (built.isSimilar(player.getInventory().getLeggings())){
                        decrement(built, player.getInventory().getLeggings(),
                                ()->player.getInventory().setLeggings(null));
                        continue;
                    }

                    if (built.isSimilar(player.getInventory().getBoots())) {
                        decrement(built, player.getInventory().getBoots(),
                                ()->player.getInventory().setBoots(null));
                        continue;
                    }

                    try {
                        if (built.isSimilar(player.getInventory().getItemInOffHand())) {
                            decrement(built, player.getInventory().getItemInOffHand(),
                                    ()->player.getInventory().setItemInOffHand(null));
                        }
                    } catch (Throwable t) { /* Ignore. Offhand missing */ }
                }
            } catch (Exception e){
                Logger.severe("Cannot remove item from player inventory: " + e.getMessage());
            }
        }
    }

    private void decrement(ItemStack item, ItemStack inSlot, Runnable clearCommand) {
        if (inSlot != null && inSlot.getAmount() > item.getAmount()) {
            inSlot.setAmount(inSlot.getAmount() - item.getAmount());
        } else {
            clearCommand.run();
        }
    }

    public static class Serializer implements NodeSerializer<ActionItemRemove> {

        @Override
        public ActionItemRemove deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            ActionItemRemove action = new ActionItemRemove();
            action.setItems(node.getList(Item.class));
            return action;
        }

    }
}
