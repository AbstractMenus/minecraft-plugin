package ru.abstractmenus.data.properties;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

public class PropNameLight implements ItemProperty {

    private final String name;

    private PropNameLight(String name){
        this.name = name;
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
        String replaced = Handlers.getPlaceholderHandler().replace(player, name);
        meta.setDisplayName(replaced);
    }

    public static class Serializer implements NodeSerializer<PropNameLight> {

        @Override
        public PropNameLight deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropNameLight(Colors.of(node.getString()));
        }

    }
}
