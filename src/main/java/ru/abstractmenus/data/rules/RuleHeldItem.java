package ru.abstractmenus.data.rules;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.data.properties.PropMaterial;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.Map;

public class RuleHeldItem implements Rule {

    private final Item item;

    private RuleHeldItem(Item item) {
        this.item = item;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        try {
            ItemStack built = item.build(player, menu);
            ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());

            Map<String, ItemProperty> properties = item.getProperties();

            if (heldItem != null && properties.size() == 1) {
                ItemProperty property = properties.values().iterator().next();
                if (property instanceof PropMaterial) {
                    return heldItem.getType() == built.getType();
                }
            }

            return built.getAmount() > 1 ? built.equals(heldItem) : built.isSimilar(heldItem);
        } catch (Exception e) {
            Logger.severe("Cannot check item in player hand: " + e.getMessage());
        }

        return false;
    }

    public static class Serializer implements NodeSerializer<RuleHeldItem> {

        @Override
        public RuleHeldItem deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleHeldItem(node.getValue(Item.class));
        }

    }
}
