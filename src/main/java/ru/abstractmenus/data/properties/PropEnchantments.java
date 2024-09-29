package ru.abstractmenus.data.properties;

import ru.abstractmenus.datatype.TypeInt;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;

import java.util.HashMap;
import java.util.Map;

public class PropEnchantments implements ItemProperty {

    private final Map<Enchantment, TypeInt> enchantments;

    private PropEnchantments(Map<Enchantment, TypeInt> enchantments){
        this.enchantments = enchantments;
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
        for (Map.Entry<Enchantment, TypeInt> entry : enchantments.entrySet()){
            meta.addEnchant(entry.getKey(), entry.getValue().getInt(player, menu), true);
        }
    }

    public static class Serializer implements NodeSerializer<PropEnchantments> {

        @Override
        public PropEnchantments deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            Map<String, ConfigNode> nodes = node.childrenMap();
            Map<Enchantment, TypeInt> map = new HashMap<>();

            for(Map.Entry<String, ConfigNode> entry : nodes.entrySet()){
                Enchantment enchantment = Enchantment.getByName(entry.getKey().toUpperCase());
                if(enchantment != null){
                    map.put(enchantment, entry.getValue().getValue(TypeInt.class));
                }
            }

            return new PropEnchantments(map);
        }

    }
}
