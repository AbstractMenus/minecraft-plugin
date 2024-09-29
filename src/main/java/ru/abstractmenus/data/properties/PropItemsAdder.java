package ru.abstractmenus.data.properties;

import dev.lone.itemsadder.api.CustomStack;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;

import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.util.bukkit.ItemUtil;

public class PropItemsAdder implements ItemProperty {

    private final String id;

    private PropItemsAdder(String id) {
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
        String id = Handlers.getPlaceholderHandler().replace(player, this.id);
        CustomStack stack = CustomStack.getInstance(id);

        if (stack == null)
            throw new IllegalArgumentException("ItemsAdder item with id '" + id + "' doesn't exists");

        ItemUtil.merge(item, stack.getItemStack());
    }

    public static class Serializer implements NodeSerializer<PropItemsAdder> {

        @Override
        public PropItemsAdder deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropItemsAdder(node.getString());
        }

    }
}
