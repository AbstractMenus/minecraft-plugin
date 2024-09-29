package ru.abstractmenus.data.properties;

import ru.abstractmenus.datatype.TypeEnum;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PropFlags implements ItemProperty {

    private final List<TypeEnum<ItemFlag>> flags;

    private PropFlags(List<TypeEnum<ItemFlag>> flags) {
        this.flags = flags;
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
        for (TypeEnum<ItemFlag> flag : flags){
            meta.addItemFlags(flag.getEnum(ItemFlag.class, player, menu));
        }
    }

    public static class Serializer implements NodeSerializer<PropFlags> {

        @Override
        public PropFlags deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if (node.isPrimitive()){
                String value = node.getString("null");
                return new PropFlags(Collections.singletonList(getItemFlag(node, value)));
            } else if (node.isList()){
                List<? extends ConfigNode> nodes = node.childrenList();
                List<TypeEnum<ItemFlag>> list = new ArrayList<>();

                for(ConfigNode flag : nodes) {
                    String value = flag.getString("null");
                    list.add(getItemFlag(flag, value));
                }

                return new PropFlags(list);
            }

            return new PropFlags(Collections.emptyList());
        }

        private TypeEnum<ItemFlag> getItemFlag(ConfigNode node, String value) throws NodeSerializeException {
            try {
                return new TypeEnum<>(ItemFlag.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e){
                if (!StringUtil.contains(value, '%'))
                    throw new NodeSerializeException(node, "ItemFlag does not contains placeholders and invalid");
                return new TypeEnum<>(value);
            }
        }

    }
}
