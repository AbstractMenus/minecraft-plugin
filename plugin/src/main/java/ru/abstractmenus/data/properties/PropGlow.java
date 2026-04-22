package ru.abstractmenus.data.properties;

import ru.abstractmenus.datatype.TypeBool;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;

public class PropGlow implements ItemProperty {

    private final TypeBool glow;

    private Enchantment durabilityEnch;

    private PropGlow(TypeBool glow){
        this.glow = glow;

        try {
            durabilityEnch = (Enchantment) Enchantment.class
                    .getField("DURABILITY")
                    .get(null);
        } catch (Throwable t) {
            // Ignore
        }
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
        if (glow.getBool(player, null)) {
            if (durabilityEnch != null) {
                meta.addEnchant(durabilityEnch, 1, true);
            } else {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            }

            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
    }

    public static class Serializer implements NodeSerializer<PropGlow> {

        @Override
        public PropGlow deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropGlow(node.getValue(TypeBool.class));
        }

    }
}
