package ru.abstractmenus.data.properties;

import ru.abstractmenus.datatype.TypeBool;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;

public class PropUnbreakable implements ItemProperty {

    private final TypeBool unbreakable;

    private PropUnbreakable(TypeBool unbreakable){
        this.unbreakable = unbreakable;
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
            meta.setUnbreakable(unbreakable.getBool(player, menu));
        } catch (Exception e) {
            /* Ignore it */
        }
    }

    public static class Serializer implements NodeSerializer<PropUnbreakable> {

        @Override
        public PropUnbreakable deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropUnbreakable(node.getValue(TypeBool.class));
        }

    }
}
