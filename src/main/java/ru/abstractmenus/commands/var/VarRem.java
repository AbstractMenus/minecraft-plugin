package ru.abstractmenus.commands.var;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.variables.VariableManagerImpl;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.api.text.Colors;

public class VarRem extends Command {

    public VarRem(){
        setUsage("&e/var rem <name> &7 - Remove global variable");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 1){
            String name = args[0];
            VariableManagerImpl.instance().deleteGlobal(name);
            sender.sendMessage(Colors.of("&aSuccessfully removed global variable '"+name+"'"));
            return;
        }
        sender.sendMessage(Colors.ofArr(getUsage()));
    }
}
