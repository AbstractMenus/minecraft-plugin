package ru.abstractmenus.data.properties;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.datatype.TypeInt;

public class PropModel implements ItemProperty {

    private final TypeInt model;

    private PropModel(TypeInt model){
        this.model = model;
    }

    @Override
    public boolean canReplaceMaterial() {
        return false;
    }

    @Override
    public boolean isApplyMeta() {
        return true;
    }

    @Override
    public void apply(ItemStack itemStack, ItemMeta meta, Player player, Menu menu) {
        try{
            meta.setCustomModelData(model.getInt(player, menu));
        } catch (Exception e){
            /* Ignore */
        }
    }

    public static class Serializer implements NodeSerializer<PropModel> {

        @Override
        public PropModel deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropModel(node.getValue(TypeInt.class));
        }

    }
}
