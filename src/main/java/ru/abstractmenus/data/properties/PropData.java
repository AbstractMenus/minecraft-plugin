package ru.abstractmenus.data.properties;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.datatype.TypeByte;

public class PropData implements ItemProperty {

    private final TypeByte data;

    private PropData(TypeByte data){
        this.data = data;
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
        itemStack.getData().setData(data.getByte(player, menu));
        itemStack.setDurability(data.getByte(player, menu));
    }

    public static class Serializer implements NodeSerializer<PropData> {

        @Override
        public PropData deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropData(node.getValue(TypeByte.class));
        }

    }
}
