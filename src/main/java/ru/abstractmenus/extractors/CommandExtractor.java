package ru.abstractmenus.extractors;

import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.command.CommandContext;

public class CommandExtractor implements ValueExtractor {

    public static final CommandExtractor INSTANCE = new CommandExtractor();

    @Override
    public String extract(Object obj, String placeholder) {
        if (obj instanceof CommandContext) {
            CommandContext ctx = (CommandContext) obj;

            if (placeholder.startsWith("cmd_arg_")) {
                String argName = placeholder.substring("cmd_arg_".length());
                return ctx.getString(argName);
            }

            switch (placeholder) {
                case "cmd_name": return ctx.getCommand().getName();
                case "cmd_args": return String.valueOf(ctx.args());
            }

            String[] args = placeholder.split(":", 2);

            if (args.length == 2) {
                String key = args[0];
                String pl = args[1];
                Object player = ctx.get(key);

                if (player != null) {
                    return PlayerExtractor.INSTANCE.extract(player, pl);
                }
            }
        }

        return "";
    }

}
