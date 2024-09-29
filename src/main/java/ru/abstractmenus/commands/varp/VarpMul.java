package ru.abstractmenus.commands.varp;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.variables.VariableManagerImpl;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.api.text.Colors;

import java.util.function.Function;

public class VarpMul extends Command {

    public VarpMul(){
        setUsage("&e/varp mul <player> <name> <value> &7 - Multiply personal variable");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 3){
            try{
                String player = args[0];
                String name = args[1];
                double value = Double.parseDouble(args[2]);
                Function<Double, Double> func = num -> num * value;
                VariableManagerImpl.instance().modifyNumericPersonal(player, name, func);
                sender.sendMessage(Colors.of("&aSuccessfully multiplied personal variable '"+name+"' for "+player+". Current value: " + VariableManagerImpl.instance().getPersonal(player, name).value()));
            } catch (NumberFormatException e){
                sender.sendMessage(Colors.of("&cNumber format error. You have to use valid number."));
            }

            return;
        }
        sender.sendMessage(Colors.ofArr(getUsage()));
    }

}
