package ru.abstractmenus.data.actions;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import java.util.List;

public class ActionItemClear implements Action {

    private List<Item> items;

    private void setItems(List<Item> items){
        this.items = items;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        for(Item item : items) {
            clear(player, menu, item);
        }
    }

    private void clear(Player player, Menu menu, Item item){
        ItemStack built = item.build(player, menu);

        for (ItemStack is : player.getInventory()){
            if(built.isSimilar(is))
                player.getInventory().remove(is);
        }

        if (built.isSimilar(player.getInventory().getHelmet()))
            player.getInventory().setHelmet(null);

        if (built.isSimilar(player.getInventory().getChestplate()))
            player.getInventory().setChestplate(null);

        if (built.isSimilar(player.getInventory().getLeggings()))
            player.getInventory().setLeggings(null);

        if (built.isSimilar(player.getInventory().getBoots()))
            player.getInventory().setBoots(null);

        try {
            if (built.isSimilar(player.getInventory().getItemInOffHand()))
                player.getInventory().setItemInOffHand(null);
        } catch (Throwable t) { /* Ignore. Offhand missing */ }
    }

    public static class Serializer implements NodeSerializer<ActionItemClear> {

        @Override
        public ActionItemClear deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            ActionItemClear action = new ActionItemClear();
            action.setItems(node.getList(Item.class));
            return action;
        }

    }
}
