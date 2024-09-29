package ru.abstractmenus.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.command.args.Argument;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.*;
import java.util.stream.Collectors;

public class Command {

    private final String name;
    private final List<String> aliases;
    private final List<Argument> args;
    private final String errorMessage;
    private final String helpPrefix;
    private final boolean override;

    private boolean playerOnly;
    private CommandHandler handler;

    public Command(String name, List<String> aliases, List<Argument> args,
                   String errorMessage, String helpPrefix, boolean override) {
        this.name = name;
        this.aliases = aliases;
        this.args = args;
        this.handler = DefaultHandler.INSTANCE;
        this.errorMessage = errorMessage;
        this.helpPrefix = helpPrefix;
        this.override = override;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean isOverride() {
        return override;
    }

    public String help() {
        return "/" + name + " " + args.stream()
                .map(Argument::help)
                .collect(Collectors.joining(" "));
    }

    public void setHandler(CommandHandler handler) {
        if (handler != null)
            this.handler = handler;
    }

    public void setPlayerOnly(boolean playerOnly) {
        this.playerOnly = playerOnly;
    }

    public void execute(CommandSender sender, List<String> args) {
        if (playerOnly && !(sender instanceof Player)) return;

        Iterator<String> iterator = args.iterator();
        CommandContext ctx = new CommandContext(this);

        for (Argument arg : this.args) {
            try {
                arg.parse(sender, iterator, ctx);
            } catch (ArgumentParseException e) {
                sender.sendMessage(String.format(errorMessage, arg.getErrorMessage()));
                sender.sendMessage(String.format(helpPrefix, help()));
                return;
            } catch (Throwable t) {
                if (arg.getDef() != null && sender instanceof Player) {
                    Player player = (Player) sender;
                    String def = Handlers.getPlaceholderHandler().replace(player, arg.getDef());

                    try {
                        Object defObj = arg.parse(sender, def);

                        if (defObj != null) {
                            ctx.add(arg.getKey(), defObj);
                            continue;
                        }
                    } catch (Throwable ignore) {}
                }

                sender.sendMessage(String.format(errorMessage, arg.getErrorMessage()));
                sender.sendMessage(String.format(helpPrefix, help()));
                return;
            }
        }

        handler.handle(sender, ctx);
    }

    public List<String> suggest(CommandSender sender, List<String> args) {
        if (playerOnly && !(sender instanceof Player)) return null;
        if (args.size() > this.args.size()) return null;

        Iterator<String> parsed = args.iterator();
        CommandContext ctx = new CommandContext(this);
        Argument arg = null;

        try {
            int i = 0;
            while (parsed.hasNext()) {
                if (i == args.size() - 1) break;
                arg = this.args.get(i);
                arg.parse(sender, parsed, ctx);
                i++;
            }

            if (parsed.hasNext()) {
                arg = this.args.get(i);
                return arg.suggest(sender);
            }
        } catch (Throwable t) {
            return Collections.singletonList(String.format(errorMessage, arg != null ? arg.getErrorMessage() : ""));
        }

        return null;
    }

    public static class Serializer implements NodeSerializer<Command> {

        @Override
        public Command deserialize(Class<Command> type, ConfigNode node) throws NodeSerializeException {
            String defMsg = "&cInvalid input: %s";
            String defHelp = "&eCommand format: %s";

            if (node.isMap()) {
                String name = node.node("name").getString();
                List<String> aliases = node.node("aliases").getList(String.class);
                String errorMessage = node.node("error").getString(defMsg);
                String helpMessage = node.node("help").getString(defHelp);
                boolean override = node.node("override").getBoolean(false);
                List<Argument> args = node.node("args").getList(Argument.class);
                return new Command(name, aliases, args, Colors.of(errorMessage),
                        Colors.of(helpMessage), override);
            }

            List<String> values = node.getList(String.class);

            if (values.isEmpty())
                throw new NodeSerializeException(node, "Command cannot be empty");

            List<String> aliases = values.size() > 1
                    ? values.subList(1, values.size())
                    : Collections.emptyList();

            return new Command(values.get(0), aliases, Collections.emptyList(),
                    Colors.of(defMsg), Colors.of(defHelp), false);
        }

    }
}
