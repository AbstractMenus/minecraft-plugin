package ru.abstractmenus.variables;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.abstractmenus.datatype.TypeBool;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

@RequiredArgsConstructor
public class VarData {

    @Getter
    private final String player;
    @Getter
    private final String name;
    @Getter
    private final String value;
    private final TypeBool replace;
    @Getter
    private final String time;

    public TypeBool isReplace() {
        return replace;
    }

    public static class Serializer implements NodeSerializer<VarData> {

        @Override
        public VarData deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if (node.isPrimitive()) {
                return parseString(node);
            } else {
                return parseMap(node);
            }
        }

        private VarData parseString(ConfigNode node) throws NodeSerializeException {
            String[] args = node.getString().split("::");

            if (args.length == 0) {
                throw new NodeSerializeException(node, "Invalid variable format");
            }

            String name = args[0];
            String value = null;
            String time = "0s";
            TypeBool replace = new TypeBool(true);

            if (name.trim().isEmpty()) {
                throw new NodeSerializeException(node, "Empty variable name");
            }

            if (args.length >= 2) {
                value = args[1];
            }

            if (args.length >= 3) {
                String arg = args[2];

                if (arg.equals("true") || arg.equals("false")) {
                    replace = new TypeBool(arg);
                } else {
                    time = arg;
                }
            }

            if (args.length >= 4)
                time = args[3];

            return new VarData(null, name, value, replace, time);
        }

        private VarData parseMap(ConfigNode node) throws NodeSerializeException {
            String player = node.node("player").getString();
            String name = node.node("name").getString();
            String value = node.node("value").getString("null");
            String time = node.node("time").getString("0s");
            TypeBool replace = node.node("replace").getValue(TypeBool.class, new TypeBool(true));

            if (name.trim().isEmpty()) {
                throw new NodeSerializeException(node.node("name"), "Empty variable name");
            }

            return new VarData(player, name, value, replace, time);
        }

    }
}