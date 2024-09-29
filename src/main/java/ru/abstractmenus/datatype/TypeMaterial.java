package ru.abstractmenus.datatype;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.abstractmenus.util.bukkit.ItemUtil;
import ru.abstractmenus.api.inventory.Menu;

public class TypeMaterial extends DataType {


    private Material value;

    public TypeMaterial(String value) {
        super(value);
    }

    public TypeMaterial(Material value) {
        super(null);
        this.value = value;
    }

    public Material getNative(){
        return value;
    }

    public Material getMaterial(Player player, Menu menu) {
        return (value != null) ? value : parseMaterial(player, menu);
    }

    private Material parseMaterial(Player player, Menu menu) {
        String replace = replaceFor(player, menu);

        try {
            return ItemUtil.getById(Integer.parseInt(replace, 10));
        } catch (NumberFormatException e){
            return Material.getMaterial(replace.toUpperCase());
        }
    }

    public static class Serializer implements NodeSerializer<TypeMaterial> {

        public TypeMaterial deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            int id = node.getInt(-1);

            if(id != -1) {
                return new TypeMaterial(ItemUtil.getById(id));
            } else {
                String value = node.getString("AIR");
                if (hasPlaceholder(value)) return new TypeMaterial(value);

                try {
                    return new TypeMaterial(Material.valueOf(value.toUpperCase()));
                } catch (Throwable t) {
                    throw new NodeSerializeException(node, "Cannot find material '" + value + "'");
                }
            }
        }

    }
}
