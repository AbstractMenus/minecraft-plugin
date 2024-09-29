package ru.abstractmenus.serializers;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Color;
import org.bukkit.DyeColor;

public class ColorSerializer implements NodeSerializer<Color> {

    @Override
    public Color deserialize(Class<Color> type, ConfigNode node) throws NodeSerializeException {
        if(node.getString() != null){
            String str = node.getString();

            if(str.charAt(0) == '#'){
                str = str.substring(1);
                int rgb = Integer.parseInt(str, 16);
                return Color.fromRGB(rgb);
            }

            String[] rgbArr = str.split(",");

            if(rgbArr.length == 3){
                int r = Integer.parseInt(rgbArr[0].trim());
                int g = Integer.parseInt(rgbArr[1].trim());
                int b = Integer.parseInt(rgbArr[2].trim());
                return Color.fromRGB(r, g, b);
            }

            return DyeColor.valueOf(node.getString()).getColor();
        }

        throw new NodeSerializeException(node, "Cannot deserialize color. You need to specify color in single string. Check our wiki");
    }

}
