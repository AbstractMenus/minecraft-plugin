package ru.abstractmenus.data.properties;

import ru.abstractmenus.datatype.TypeInt;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;

import java.util.List;

public class PropFireworkData implements ItemProperty {

    private final TypeInt power;
    private final List<FireworkEffect> effects;

    private PropFireworkData(TypeInt power, List<FireworkEffect> effects){
        this.power = power;
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
        if(meta instanceof FireworkMeta){
            FireworkMeta fireworkMeta = (FireworkMeta) meta;
            fireworkMeta.setPower(power.getInt(player, menu));
            fireworkMeta.addEffects(effects);
        }
    }

    public static class Serializer implements NodeSerializer<PropFireworkData> {

        @Override
        public PropFireworkData deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            TypeInt power = node.node("power").getValue(TypeInt.class, new TypeInt(1));
            List<FireworkEffect> effects = node.node("effects").getList(FireworkEffect.class);
            return new PropFireworkData(power, effects);
        }

    }
}
