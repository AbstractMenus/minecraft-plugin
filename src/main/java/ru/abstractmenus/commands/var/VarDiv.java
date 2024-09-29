package ru.abstractmenus.commands.var;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.variables.VariableManagerImpl;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.api.text.Colors;

import java.util.function.Function;

public class VarDiv extends Command {

    public VarDiv() {
        setUsage("&e/var mul <name> <value> &7 - Divide global variable");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 2) {
            try {
                double value = Double.parseDouble(args[1]);
                if (value == 0) {
                    sender.sendMessage(Colors.of("&cYou cannot divide by zero!"));
                    return;
                }
                String name = args[0];
                Function<Double, Double> func = num -> num / value;
                VariableManagerImpl.instance().modifyNumericGlobal(name, func);
                sender.sendMessage(Colors.of("&aSuccessfully divided global variable '"+name+"'. Current value: " + VariableManagerImpl.instance().getGlobal(name).value()));
            } catch (NumberFormatException e){
                sender.sendMessage(Colors.of("&cNumber format error. You have to use valid number."));
            }

            return;
        }
        sender.sendMessage(Colors.ofArr(getUsage()));
    }

}
