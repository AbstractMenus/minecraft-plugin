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

import java.util.List;

public class PropLoreLight implements ItemProperty {

    private final List<String> lore;

    private PropLoreLight(List<String> lore){
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
        meta.setLore(replaced);
    }

    public static class Serializer implements NodeSerializer<PropLoreLight> {

        @Override
        public PropLoreLight deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropLoreLight(Colors.ofList(node.getList(String.class)));
        }

    }
}
