package ru.abstractmenus.data.properties;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;

import java.util.List;

public class PropPotionData implements ItemProperty {

    private final List<PotionEffect> effects;

    private PropPotionData(List<PotionEffect> effects){
        this.effects = effects;
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
        if(meta instanceof PotionMeta) {
            for(PotionEffect effect : effects) {
                ((PotionMeta) meta).addCustomEffect(effect, true);
            }
        }
    }

    public static class Serializer implements NodeSerializer<PropPotionData> {

        @Override
        public PropPotionData deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropPotionData(node.getList((PotionEffect.class)));
        }

    }
}
