package ru.abstractmenus.commands.varp;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.api.variables.Var;
import ru.abstractmenus.variables.VariableManagerImpl;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.api.text.Colors;

public class VarpGet extends Command {

    public VarpGet(){
        setUsage("&e/varp get <player> <name> &7 - Get value of personal variable");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 2){
            String player = args[0];
            String name = args[1];
            Var var = VariableManagerImpl.instance().getPersonal(player, name);

            if(var != null){
                sender.sendMessage(Colors.of("&aValue of personal "+player+" variable '"+name+"': " + var.value()));
            } else{
                sender.sendMessage(Colors.of("&cVariable not found"));
            }

            return;
        }
        sender.sendMessage(Colors.ofArr(getUsage()));
    }

}
