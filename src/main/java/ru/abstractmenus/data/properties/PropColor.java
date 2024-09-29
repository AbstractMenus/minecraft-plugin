package ru.abstractmenus.data.properties;

import ru.abstractmenus.datatype.TypeColor;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;

public class PropColor implements ItemProperty {

    private final TypeColor color;

    private PropColor(TypeColor color){
        this.color = color;
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
        if (meta instanceof LeatherArmorMeta){
            ((LeatherArmorMeta)meta).setColor(color.getColor(player, menu));
        } else if (meta instanceof PotionMeta){
            ((PotionMeta)meta).setColor(color.getColor(player, menu));
        }
    }

    public static class Serializer implements NodeSerializer<PropColor> {

        @Override
        public PropColor deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropColor(node.getValue(TypeColor.class));
        }

    }
}
