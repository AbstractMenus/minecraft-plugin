package ru.abstractmenus.serializers;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectSerializer implements NodeSerializer<PotionEffect> {

    @Override
    public PotionEffect deserialize(Class<PotionEffect> nodeType, ConfigNode node) throws NodeSerializeException {
        String name = node.node("effectType").getString();
        PotionEffectType type = PotionEffectType.getByName(name);

        if(type == null){
            throw new NodeSerializeException(node.node("effectType"), "Potion effect type '"+name+"' does not exist");
        }

        int duration = node.node("duration").getInt();
        int amplifier = node.node("amplifier").getInt();
        return new PotionEffect(type, duration, amplifier);
    }

}
