package ru.abstractmenus.command.args;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.util.NumberUtil;

public class NumberArgument extends Argument {

    public NumberArgument(String key) {
        super(key, Colors.of("&cInvalid number"));
    }

    @Override
    public Object parse(CommandSender sender, String value) {
        return NumberUtil.tryToInt(Double.parseDouble(value));
    }

    public static class Serializer implements NodeSerializer<NumberArgument> {

        @Override
        public NumberArgument deserialize(Class<NumberArgument> type, ConfigNode node) throws NodeSerializeException {
            return new NumberArgument(node.node("key").getString());
        }

    }

}
