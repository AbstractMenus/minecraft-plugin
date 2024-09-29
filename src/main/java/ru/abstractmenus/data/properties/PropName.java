package ru.abstractmenus.data.properties;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.util.MiniMessageUtil;

public class PropName implements ItemProperty {

    private final String name;

    private PropName(String name){
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
        String formatted = MiniMessageUtil.parseToLegacy(replaced);

        meta.setDisplayName(formatted);
    }

    public static class Serializer implements NodeSerializer<PropName> {

        @Override
        public PropName deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropName(Colors.of(node.getString()));
        }

    }
}
