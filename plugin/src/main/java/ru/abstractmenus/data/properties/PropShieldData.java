package ru.abstractmenus.data.properties;

import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.data.BannerData;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

public class PropShieldData implements ItemProperty {

    private final BannerData data;

    private PropShieldData(BannerData data){
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
        if (meta instanceof BlockStateMeta){
            BlockStateMeta bmeta = ((BlockStateMeta)meta);
            BlockState state = bmeta.getBlockState();

            if (state instanceof Banner) {
                Banner banner = (Banner) state;
                banner.setPatterns(data.getPatterns());
                banner.update();
                bmeta.setBlockState(state);
            }
        }
    }

    public static class Serializer implements NodeSerializer<PropShieldData> {

        @Override
        public PropShieldData deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropShieldData(node.getValue(BannerData.class));
        }

    }
}
