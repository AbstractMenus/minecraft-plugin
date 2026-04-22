package ru.abstractmenus.data.properties;

import ru.abstractmenus.data.BannerData;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;

public class PropBannerData implements ItemProperty {

    private final BannerData data;

    private PropBannerData(BannerData data){
        this.data = data;
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
        if (meta instanceof BannerMeta){
            ((BannerMeta)meta).setPatterns(data.getPatterns());
        }
    }

    public static class Serializer implements NodeSerializer<PropBannerData> {

        @Override
        public PropBannerData deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropBannerData(node.getValue(BannerData.class));
        }

    }
}
