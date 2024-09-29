package ru.abstractmenus.data.properties;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.inventory.Menu;

import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.util.bukkit.ItemUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class PropOraxen implements ItemProperty {

    private static MethodHandle getItemByIdMethod;
    private static MethodHandle buildMethod;

    private final String id;

    static {
        try {
            Class<?> oraxenClass = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
            Class<?> itemBuilderClass = Class.forName("io.th0rgal.oraxen.items.ItemBuilder");
            Method getItemById = oraxenClass.getMethod("getItemById", String.class);
            Method build = itemBuilderClass.getDeclaredMethod("build");

            getItemByIdMethod = MethodHandles.lookup().unreflect(getItemById);
            buildMethod = MethodHandles.lookup().unreflect(build);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private PropOraxen(String id) {
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
        ItemStack oraxenItem = getItem(player);

        if (oraxenItem != null) {
            ItemUtil.merge(item, oraxenItem);
        }
    }

    private ItemStack getItem(Player player) {
        String id = Handlers.getPlaceholderHandler().replace(player, this.id);
        try {
            Object builder = getItemByIdMethod.invoke(id);
            if (builder == null)
                throw new IllegalArgumentException("Invalid Oraxen item id");
            return (ItemStack) buildMethod.invoke(builder);
        } catch (Throwable t) {
            Logger.severe("Cannot build Oraxen item");
        }
        return null;
    }

    public static class Serializer implements NodeSerializer<PropOraxen> {

        @Override
        public PropOraxen deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropOraxen(node.getString());
        }

    }
}
