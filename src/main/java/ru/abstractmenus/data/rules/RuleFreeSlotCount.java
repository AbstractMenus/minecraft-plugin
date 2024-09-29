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

public class RuleFreeSlotCount implements Rule {

    private final TypeInt count;

    private RuleFreeSlotCount(TypeInt count){
        this.count = count;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        int i = 0;

        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType().equals(Material.AIR))
                i++;
        }

        return i >= count.getInt(player, menu);
    }

    public static class Serializer implements NodeSerializer<RuleFreeSlotCount> {

        @Override
        public RuleFreeSlotCount deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleFreeSlotCount(node.getValue(TypeInt.class));
        }

    }
}
