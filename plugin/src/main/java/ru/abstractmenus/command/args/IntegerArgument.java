package ru.abstractmenus.command.args;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

public class IntegerArgument extends Argument {

    public IntegerArgument(String key) {
        super(key, Colors.of("&cInvalid integer"));
    }

    @Override
    public Object parse(CommandSender sender, String value) {
        return Integer.parseInt(value);
    }

    public static class Serializer implements NodeSerializer<IntegerArgument> {

        @Override
        public IntegerArgument deserialize(Class<IntegerArgument> type, ConfigNode node) throws NodeSerializeException {
            return new IntegerArgument(node.node("key").getString());
        }

    }

}
