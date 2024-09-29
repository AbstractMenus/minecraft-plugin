package ru.abstractmenus.data.properties;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
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
import ru.abstractmenus.util.bukkit.Skulls;

public class PropHDB implements ItemProperty {

    private final String id;

    private PropHDB(String id){
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
        String replaced = Handlers.getPlaceholderHandler().replace(player, id);
        ItemStack headItem = new HeadDatabaseAPI().getItemHead(replaced);

        if (headItem == null)
            headItem = Skulls.createSkullItem();

        ItemUtil.merge(item, headItem);
    }

    public static class Serializer implements NodeSerializer<PropHDB> {

        @Override
        public PropHDB deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropHDB(node.getString());
        }

    }
}
