package ru.abstractmenus.variables;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.datatype.TypeDouble;

public class VarNumData {

    private final String player;
    private final String name;
    private final TypeDouble value;

    public VarNumData(String player, String name, TypeDouble value) {
        this.player = player;
        this.name = name;
        this.value = value;
    }

    public String getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

    public TypeDouble getValue() {
        return value;
    }

    public static class Serializer implements NodeSerializer<VarNumData> {

        @Override
        public VarNumData deserialize(Class<VarNumData> type, ConfigNode node) throws NodeSerializeException {
            if (node.isPrimitive()) {
                return parseString(node);
            } else {
                return parseMap(node);
            }
        }

        private VarNumData parseString(ConfigNode node) throws NodeSerializeException {
            String[] args = node.getString().split("::");

            if (args.length >= 2) {
                String name = args[0];
                TypeDouble value = new TypeDouble(args[1]);
                return new VarNumData(null, name, value);
            }

            throw new NodeSerializeException(node, "Invalid numeric variable format");
        }

        private VarNumData parseMap(ConfigNode node) throws NodeSerializeException {
            String player = node.node("player").getString();
            String name = node.node("name").getString();
            TypeDouble value = node.node("value").getValue(TypeDouble.class);
            return new VarNumData(player, name, value);
        }

    }
}
