package ru.abstractmenus.data.properties;

import ru.abstractmenus.datatype.TypeInt;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;

public class PropCount implements ItemProperty {

    private final TypeInt amount;

    private PropCount(TypeInt amount){
        this.amount = amount;
    }

    @Override
    public boolean canReplaceMaterial() {
        return false;
    }

    @Override
    public boolean isApplyMeta() {
        return false;
    }

    @Override
    public void apply(ItemStack itemStack, ItemMeta meta, Player player, Menu menu) {
        itemStack.setAmount(amount.getInt(player, menu));
        
    }

    public static class Serializer implements NodeSerializer<PropCount> {

        @Override
        public PropCount deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropCount(node.getValue(TypeInt.class));
        }

    }
}
