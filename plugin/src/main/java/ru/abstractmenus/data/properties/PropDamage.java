package ru.abstractmenus.data.properties;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.datatype.TypeInt;

public class PropDamage implements ItemProperty {

    private final TypeInt damage;

    private PropDamage(TypeInt damage) {
        this.damage = damage;
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
        if (meta instanceof Damageable damageable) {
            damageable.setDamage(damage.getInt(player, menu));
        }
    }

    public static class Serializer implements NodeSerializer<PropDamage> {

        @Override
        public PropDamage deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropDamage(node.getValue(TypeInt.class));
        }

    }
}
