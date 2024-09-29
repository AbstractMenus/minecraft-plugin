package ru.abstractmenus.data.properties;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.util.NMS;

public class PropNbt implements ItemProperty {

    private final NBTCompound nbt;

    private PropNbt(NBTCompound nbt) {
        this.nbt = nbt;
    }

    @Override
    public boolean canReplaceMaterial() {
        return false;
    }

    @Override
    public boolean isApplyMeta() {
        return false;
    }

    @Override
    public void apply(ItemStack item, ItemMeta meta, Player player, Menu menu) {
        NBTItem nbti = new NBTItem(item);
        nbti.mergeCompound(this.nbt);
        ItemStack nbtItem = nbti.getItem();
        item.setType(nbtItem.getType());

        if (NMS.getMinorVersion() <= 12)
            item.setData(nbtItem.getData());

        item.setAmount(nbtItem.getAmount());
        item.setItemMeta(nbtItem.getItemMeta());
    }

    public static class Serializer implements NodeSerializer<PropNbt> {

        @Override
        public PropNbt deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropNbt(node.getValue(NBTCompound.class));
        }

    }
}
