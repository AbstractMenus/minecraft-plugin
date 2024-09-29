package ru.abstractmenus.commands;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.api.text.Colors;

public class VarpCommand extends Command {

    public VarpCommand(String permission){
        super(permission);
        setUsage("&e/varp get <player> <name> &7- Get personal variable value",
                "&e/varp set <player> <name> <value> &7- Set personal variable",
                "&e/varp set <player> <name> <value> <time> &7- Set personal variable",
                "&e/varp set <player> <name> <value> <time> <replace> &7- Set personal variable",
                "&e/varp rem <player> <name> &7- Remove personal variable",
                "&e/varp inc <player> <name> <value> &7- Increment personal variable",
                "&e/varp dec <player> <name> <value> &7- Decrement personal variable",
                "&e/varp mul <player> <name> <value> &7- Multiply personal variable",
                "&e/varp div <player> <name> <value> &7- Divide personal variable");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(Colors.ofArr(getUsage()));
    }
}
