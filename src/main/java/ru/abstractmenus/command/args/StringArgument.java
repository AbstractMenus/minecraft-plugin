package ru.abstractmenus.command.args;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

public class StringArgument extends Argument {

    public StringArgument(String key) {
        super(key, Colors.of("&cInvalid string"));
    }

    @Override
    public Object parse(CommandSender sender, String value) {
        return value;
    }

    public static class Serializer implements NodeSerializer<StringArgument> {

        @Override
        public StringArgument deserialize(Class<StringArgument> type, ConfigNode node) throws NodeSerializeException {
            return new StringArgument(node.node("key").getString());
        }

    }

}
