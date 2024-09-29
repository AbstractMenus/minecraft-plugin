package ru.abstractmenus.data.properties;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.util.bukkit.ItemUtil;
import ru.abstractmenus.api.inventory.Menu;

import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.util.bukkit.Skulls;

public class PropSkullOwner implements ItemProperty {

    private final String owner;

    private PropSkullOwner(String owner) {
        this.owner = owner;
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
        String replaced = Handlers.getPlaceholderHandler().replace(player, owner);
        ItemStack skullItem = Skulls.getPlayerSkull(replaced);

        if (skullItem == null) {
            skullItem = Skulls.createSkullItem();
        }

        ItemUtil.merge(item, skullItem);

        //Logger.severe("Cannot get head of player " + replaced + ". Item is null");
    }

    public static class Serializer implements NodeSerializer<PropSkullOwner> {

        @Override
        public PropSkullOwner deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropSkullOwner(node.getString());
        }

    }
}
