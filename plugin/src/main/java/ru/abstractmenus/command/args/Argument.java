package ru.abstractmenus.command.args;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.command.ArgumentParseException;
import ru.abstractmenus.command.CommandContext;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Getter
public abstract class Argument {

    private final String key;
    @Setter
    private String def;
    @Setter
    private String errorMessage;

    public Argument(String key, String errorMessage) {
        this.key = key;
        this.errorMessage = errorMessage;
    }

    public String help() {
        return def == null ? "<"+key+">" : "["+key+"]";
    }

    public List<String> suggest(CommandSender sender) {
        return Collections.singletonList(help());
    }

    public void parse(CommandSender sender, Iterator<String> args, CommandContext ctx) {
        String next = args.next();
        Object value = parse(sender, next);

        if (value == null)
            throw new ArgumentParseException("Invalid argument");

        ctx.add(getKey(), value);
    }

    public abstract Object parse(CommandSender sender, String value);

    public static class Serializer implements NodeSerializer<Argument> {

        @Override
        public Argument deserialize(Class<Argument> type, ConfigNode node) throws NodeSerializeException {
            if (node.isPrimitive()) {
                return new StringArgument(node.getString());
            }

            String argType = node.node("type").getString("string");
            Argument argument = switch (argType.toLowerCase()) {
                case "string" -> node.getValue(StringArgument.class);
                case "number" -> node.getValue(NumberArgument.class);
                case "integer" -> node.getValue(IntegerArgument.class);
                case "choice" -> node.getValue(ChoiceArgument.class);
                case "player" -> node.getValue(PlayerArgument.class);
                default -> throw new NodeSerializeException(node, "Undefined command argument type");
            };

            String errorMessage = node.node("error").getString();
            String def = node.node("default").getString();

            if (errorMessage != null)
                argument.setErrorMessage(Colors.of(errorMessage));

            if (def != null)
                argument.setDef(Colors.of(def));

            return argument;
        }

    }
}
