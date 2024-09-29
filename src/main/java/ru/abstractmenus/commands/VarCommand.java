package ru.abstractmenus.commands;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.api.text.Colors;

public class VarCommand extends Command {

    public VarCommand(String permission){
        super(permission);
        setUsage("&e/var get <name> &7- Get global variable value",
                "&e/var set <name> <value> &7- Set global variable",
                "&e/var set <name> <value> <time> &7- Set global variable",
                "&e/var set <name> <value> <time> <replace> &7- Set global variable",
                "&e/var rem <name> &7- Remove global variable",
                "&e/var inc <name> <value> &7- Increment global variable",
                "&e/var dec <name> <value> &7- Decrement global variable",
                "&e/var mul <name> <value> &7- Multiply global variable",
                "&e/var div <name> <value> &7- Divide global variable");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(Colors.ofArr(getUsage()));
    }
}
