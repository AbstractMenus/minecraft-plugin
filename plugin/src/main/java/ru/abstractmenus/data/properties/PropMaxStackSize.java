package ru.abstractmenus.data.properties;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.datatype.DataType;
import ru.abstractmenus.datatype.TypeInt;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

public class PropMaxStackSize implements ItemProperty {

    private final TypeInt maxStackSize;

    private PropMaxStackSize(TypeInt maxStackSize) {
        this.maxStackSize = maxStackSize;
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
        meta.setMaxStackSize(maxStackSize.getInt(player, menu));
    }

    public static class Serializer implements NodeSerializer<PropMaxStackSize> {

        @Override
        public PropMaxStackSize deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            TypeInt value = new TypeInt.Serializer().deserialize(TypeInt.class, node);
            if (!DataType.hasPlaceholder(node.getString(""))) {
                int size = value.getInt(null, null);
                if (size < 1 || size > 99) {
                    throw new NodeSerializeException(node, "maxStackSize must be between 1 and 99");
                }
            }
            return new PropMaxStackSize(value);
        }
    }
}
