package ru.abstractmenus.data.properties;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.datatype.DataType;
import ru.abstractmenus.datatype.TypeEnum;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.Locale;

public class PropRarity implements ItemProperty {

    private final TypeEnum<ItemRarity> rarity;

    private PropRarity(TypeEnum<ItemRarity> rarity) {
        this.rarity = rarity;
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
        meta.setRarity(rarity.getEnum(ItemRarity.class, player, menu));
    }

    public static class Serializer implements NodeSerializer<PropRarity> {

        @Override
        public PropRarity deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            String value = node.getString("");
            if (DataType.hasPlaceholder(value)) {
                return new PropRarity(new TypeEnum<>(value));
            }
            try {
                return new PropRarity(new TypeEnum<>(ItemRarity.valueOf(value.toUpperCase(Locale.ROOT))));
            } catch (IllegalArgumentException e) {
                throw new NodeSerializeException(node, "Unknown item rarity '" + value + "'");
            }
        }
    }
}
