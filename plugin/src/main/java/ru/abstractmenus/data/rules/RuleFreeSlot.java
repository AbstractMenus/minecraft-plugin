package ru.abstractmenus.data.rules;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.datatype.TypeInt;

public class RuleFreeSlot implements Rule {

    private final TypeInt slot;

    private RuleFreeSlot(TypeInt slot){
        this.slot = slot;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        int slot = this.slot.getInt(player, menu);

        if (slot < 0) {
            for (ItemStack item : player.getInventory().getStorageContents()) {
                if (item == null || item.getType().equals(Material.AIR))
                    return true;
            }
            return false;
        }

        return player.getInventory().getItem(slot) == null;
    }

    public static class Serializer implements NodeSerializer<RuleFreeSlot> {

        @Override
        public RuleFreeSlot deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleFreeSlot(node.getValue(TypeInt.class));
        }
    }
}
