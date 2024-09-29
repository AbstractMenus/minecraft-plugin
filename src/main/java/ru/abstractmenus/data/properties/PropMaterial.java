package ru.abstractmenus.data.properties;

import ru.abstractmenus.datatype.TypeMaterial;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;

import ru.abstractmenus.api.inventory.ItemProperty;

public class PropMaterial implements ItemProperty {

    private final TypeMaterial material;

    private PropMaterial(TypeMaterial material){
        this.material = material;
    }

    @Override
    public boolean canReplaceMaterial() {
        return true;
    }

    @Override
    public boolean isApplyMeta() {
        return false;
    }

    @Override
    public void apply(ItemStack item, ItemMeta meta, Player player, Menu menu) {
        item.setType(material.getMaterial(player, menu));
    }

    public static class Serializer implements NodeSerializer<PropMaterial> {

        @Override
        public PropMaterial deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            TypeMaterial material = node.getValue(TypeMaterial.class);
            if(material != null && material.getNative() != null) checkMaterial(node, material.getNative());
            return new PropMaterial(material);
        }

        private void checkMaterial(ConfigNode node, Material material) throws NodeSerializeException {
            if(node.node("material").rawValue() != null && material == null){
                throw new NodeSerializeException(node, "Material with id " + node.node("material").getString() + " does not exist");
            }
        }

    }
}
