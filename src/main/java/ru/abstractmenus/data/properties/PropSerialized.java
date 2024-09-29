package ru.abstractmenus.data.properties;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.util.bukkit.ItemUtil;

public class PropSerialized implements ItemProperty {

    private final String value;

    private PropSerialized(String value){
        this.value = value;
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
        String base64 = Handlers.getPlaceholderHandler().replace(player, value);
        ItemStack deserialized = ItemUtil.decodeStack(base64);

        if (deserialized == null) {
            deserialized = ItemUtil.empty();
            Logger.severe("Cannot deserialize item from base64. Replaced to AIR");
        }

        ItemUtil.merge(item, deserialized);
    }

    public static class Serializer implements NodeSerializer<PropSerialized> {

        @Override
        public PropSerialized deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropSerialized(node.getString());
        }

    }
}
