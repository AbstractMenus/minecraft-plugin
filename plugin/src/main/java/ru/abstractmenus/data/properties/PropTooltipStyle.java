package ru.abstractmenus.data.properties;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

public class PropTooltipStyle implements ItemProperty {

    private final NamespacedKey tooltipStyle;

    private PropTooltipStyle(NamespacedKey tooltipStyle) {
        this.tooltipStyle = tooltipStyle;
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
        meta.setTooltipStyle(tooltipStyle);
    }

    public static class Serializer implements NodeSerializer<PropTooltipStyle> {

        @Override
        public PropTooltipStyle deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            NamespacedKey key = NamespacedKey.fromString(node.getString(""));
            if (key == null) {
                throw new NodeSerializeException(node, "Invalid namespaced key");
            }
            return new PropTooltipStyle(key);
        }
    }
}
