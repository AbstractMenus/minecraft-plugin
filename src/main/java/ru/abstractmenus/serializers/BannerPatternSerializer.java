package ru.abstractmenus.serializers;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

public class BannerPatternSerializer implements NodeSerializer<Pattern> {

    @Override
    public Pattern deserialize(Class<Pattern> token, ConfigNode node) throws NodeSerializeException {
        String typeStr = node.node("type").getString("null").toUpperCase();
        String colorStr = node.node("color").getString("null").toUpperCase();

        PatternType type;
        DyeColor color;

        try {
            type = PatternType.valueOf(typeStr);
        } catch (IllegalArgumentException e){
            throw new NodeSerializeException(node.node("type"), "Cannot find banner pattern type: " + typeStr);
        }

        try{
            color = DyeColor.valueOf(colorStr);
        } catch (IllegalArgumentException e){
            throw new NodeSerializeException(node.node("color"), "Cannot find pattern color: " + colorStr);
        }

        return new Pattern(color, type);
    }
}
