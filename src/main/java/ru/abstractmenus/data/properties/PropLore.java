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

import java.util.List;

public class PropLore implements ItemProperty {

    private final List<String> lore;

    private PropLore(List<String> lore){
        this.lore = lore;
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
        List<String> replaced = Handlers.getPlaceholderHandler().replace(player, lore);
        List<String> formatted = MiniMessageUtil.parseToLegacy(replaced);
        meta.setLore(formatted);
    }

    public static class Serializer implements NodeSerializer<PropLore> {

        @Override
        public PropLore deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropLore(Colors.ofList(node.getList(String.class)));
        }

    }
}
