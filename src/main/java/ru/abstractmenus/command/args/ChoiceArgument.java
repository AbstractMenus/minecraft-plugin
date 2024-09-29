package ru.abstractmenus.command.args;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import java.util.List;

public class ChoiceArgument extends Argument {

    private final List<String> options;

    public ChoiceArgument(String key, List<String> options) {
        super(key, Colors.of("&cInvalid choice"));
        this.options = options;
    }

    @Override
    public List<String> suggest(CommandSender sender) {
        return options;
    }

    @Override
    public Object parse(CommandSender sender, String value) {
        if (!options.contains(value))
            throw new IllegalArgumentException("Invalid choice");

        return value;
    }

    public static class Serializer implements NodeSerializer<ChoiceArgument> {

        @Override
        public ChoiceArgument deserialize(Class<ChoiceArgument> type, ConfigNode node) throws NodeSerializeException {
            String key = node.node("key").getString();
            List<String> options = node.node("options").getList(String.class);
            return new ChoiceArgument(key, options);
        }

    }

}
