package ru.abstractmenus.data.properties;

import net.Indyuce.mmoitems.MMOItems;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;

import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.util.bukkit.ItemUtil;

import java.util.Arrays;

public class PropMmoItem implements ItemProperty {

    private final String id;

    private PropMmoItem(String id) {
        this.id = id;
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
        String[] arr = Handlers.getPlaceholderHandler().replace(player, id).split(":");

        if(arr.length >= 2) {
            ItemStack mmoItem = MMOItems.plugin.getItem(MMOItems.plugin.getTypes().get(arr[0]), arr[1]);

            if (mmoItem != null) {
                ItemUtil.merge(item, mmoItem);
                return;
            }
        }

        Logger.severe("MMOItem with id '" + Arrays.toString(arr) + "' not found. Skipped");
    }

    public static class Serializer implements NodeSerializer<PropMmoItem> {

        @Override
        public PropMmoItem deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropMmoItem(node.getString());
        }

    }
}
