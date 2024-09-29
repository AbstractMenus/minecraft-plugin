package ru.abstractmenus.datatype;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.inventory.Menu;

public class TypeColor extends DataType {

    private Color value;

    public TypeColor(Color value){
        super(null);
        this.value = value;
    }

    public TypeColor(String value) {
        super(value);
    }

    public Color getColor(Player player, Menu menu) {
        try {
            return value != null ? value : parseColor(player, menu);
        } catch (IllegalArgumentException e) {
            Logger.severe("Cannot parse color from string: " + e.getMessage());
            return Color.WHITE;
        }
    }

    private Color parseColor(Player player, Menu menu) throws IllegalArgumentException {
        if(getValue() != null){
            String str = replaceFor(player, menu);

            if(str.charAt(0) == '#'){
                str = str.substring(1);
                int rgb = Integer.parseInt(str, 16);
                return Color.fromRGB(rgb);
            }

            String[] rgbArr = str.split(",");

            if(rgbArr.length == 3){
                return Color.fromRGB(
                        Integer.parseInt(rgbArr[0].trim(), 10),
                        Integer.parseInt(rgbArr[1].trim(), 10),
                        Integer.parseInt(rgbArr[2].trim(), 10)
                );
            }

            return DyeColor.valueOf(str).getColor();
        }

        throw new IllegalArgumentException("Cannot deserialize color. You need to specify color in single string. Check our wiki");
    }

    public static class Serializer implements NodeSerializer<TypeColor> {

        @Override
        public TypeColor deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if(node.isPrimitive()) {
                String str = node.getString();

                if (hasPlaceholder(str))
                    return new TypeColor(str);

                if(str.charAt(0) == '#') {
                    int rgb = Integer.parseInt(str.substring(1), 16);
                    return new TypeColor(Color.fromRGB(rgb));
                }

                String[] rgbArr = str.split(",");

                if(rgbArr.length == 3){
                    int r = Integer.parseInt(rgbArr[0].trim());
                    int g = Integer.parseInt(rgbArr[1].trim());
                    int b = Integer.parseInt(rgbArr[2].trim());
                    return new TypeColor(Color.fromRGB(r, g, b));
                }

                try {
                    return new TypeColor(DyeColor.valueOf(str.toUpperCase()).getColor());
                } catch (Exception e) {
                    throw new NodeSerializeException(node, "No Color constant '"+str.toUpperCase()+"'");
                }
            }

            throw new IllegalArgumentException("Cannot deserialize color. You need to specify color in single string. Check our wiki");
        }
    }
}
