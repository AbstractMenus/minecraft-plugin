package ru.abstractmenus.commands.varp;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.variables.VariableManagerImpl;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.api.text.Colors;

import java.util.function.Function;

public class VarpDiv extends Command {

    public VarpDiv(){
        setUsage("&e/varp div <player> <name> <value> &7 - Divide personal variable");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 3){
            try {
                String player = args[0];
                String name = args[1];
                double value = Double.parseDouble(args[2]);

                if (value == 0) {
                    sender.sendMessage(Colors.of("&7You cannot divide by zero"));
                    return;
                }

                Function<Double, Double> func = num -> num / value;
                VariableManagerImpl.instance().modifyNumericPersonal(player, name, func);
                sender.sendMessage(Colors.of("&aSuccessfully divided personal variable '"+name+"' for "+player+". Current value: " + VariableManagerImpl.instance().getPersonal(player, name).value()));
            } catch (NumberFormatException e) {
                sender.sendMessage(Colors.of("&cNumber format error. You have to use valid number."));
            }

            return;
        }
        sender.sendMessage(Colors.ofArr(getUsage()));
    }

}
