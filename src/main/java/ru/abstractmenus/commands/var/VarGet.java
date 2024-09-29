package ru.abstractmenus.commands.var;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.api.variables.Var;
import ru.abstractmenus.variables.VariableManagerImpl;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.api.text.Colors;

public class VarGet extends Command {

    public VarGet() {
        setUsage("&e/var get <name> &7 - Get value of global variable");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 1) {
            String name = args[0];
            Var var = VariableManagerImpl.instance().getGlobal(name);

            if(var != null) {
                sender.sendMessage(Colors.of("&aValue of global variable '"+name+"': " + var.value()));
            } else{
                sender.sendMessage(Colors.of("&cVariable not found"));
            }

            return;
        }
        sender.sendMessage(Colors.ofArr(getUsage()));
    }
}
