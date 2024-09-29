package ru.abstractmenus.serializers;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;

public class FireworkEffectSerializer implements NodeSerializer<FireworkEffect> {

    @Override
    public FireworkEffect deserialize(Class<FireworkEffect> typeToken, ConfigNode node) throws NodeSerializeException {
        FireworkEffect.Builder builder = FireworkEffect.builder();

        if(node.node("colors").rawValue() != null){
            builder.withColor(node.node("colors").getList(Color.class));
        }

        if(node.node("fadeColors").rawValue() != null){
            builder.withFade(node.node("fadeColors").getList(Color.class));
        }

        if(node.node("trail").rawValue() != null){
            builder.trail(node.node("trail").getBoolean());
        }

        builder.with(FireworkEffect.Type.valueOf(node.node("type").getString()));

        return builder.build();
    }

}
