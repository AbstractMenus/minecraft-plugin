package ru.abstractmenus.commands.varp;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.variables.VariableManagerImpl;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.api.text.Colors;

public class VarpRem extends Command {

    public VarpRem(){
        setUsage("&e/varp rem <player> <name> &7 - Remove personal variable");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 2){
            String player = args[0];
            String name = args[1];

            VariableManagerImpl.instance().deletePersonal(player, name);
            sender.sendMessage(Colors.of("&aSuccessfully removed personal variable '"+name+"' for player "+player+""));
            return;
        }
        sender.sendMessage(Colors.ofArr(getUsage()));
    }

}
