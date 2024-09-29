package ru.abstractmenus.commands.var;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.api.variables.Var;
import ru.abstractmenus.variables.VariableManagerImpl;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.util.TimeUtil;

public class VarSet extends Command {

    public VarSet(){
        setUsage("/var set <name> <value> - Set global variable",
                "/var set <name> <value> <replace> - Set global variable with replace protection",
                "/var set <name> <value> <time> - Set global temporary variable",
                "/var set <name> <value> <time> <replace> - Set global temporary variable with replace protection");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length >= 2) {
            String name = args[0];
            String value = args[1];

            Var var = VariableManagerImpl.instance().createBuilder()
                    .name(name)
                    .value(value)
                    .build();

            if(args.length == 2) {
                // just set var
                VariableManagerImpl.instance().saveGlobal(var);
                sender.sendMessage(Colors.of("&aSuccessfully set value '"+value+"' to global variable '"+name+"'"));
                return;
            }
            if(args.length == 3){
                if(args[2].equals("true") || args[2].equals("false")){
                    // set replace var
                    boolean replace = Boolean.parseBoolean(args[2]);
                    VariableManagerImpl.instance().saveGlobal(var, replace);
                    sender.sendMessage(Colors.of("&aSuccessfully set value '"+value+"' to global variable '"+name+"' (replace="+replace+")"));
                    return;
                }
                // set time var
                try {
                    long time = TimeUtil.parseTime(args[2]);
                    var = var.toBuilder()
                            .expiry(System.currentTimeMillis() + time)
                            .build();
                    VariableManagerImpl.instance().saveGlobal(var);
                    sender.sendMessage(Colors.of("&aSuccessfully set value '"+value+"' to global temporary variable '"+name+"'"));
                } catch (Exception e){
                    sender.sendMessage(Colors.of("&cInvalid time format!"));
                }
                return;
            }
            if(args.length == 4){
                // set time replace var
                try{
                    long time = TimeUtil.parseTime(args[2]);
                    boolean replace = Boolean.parseBoolean(args[3]);
                    var = var.toBuilder()
                            .expiry(System.currentTimeMillis() + time)
                            .build();
                    VariableManagerImpl.instance().saveGlobal(var, replace);
                    sender.sendMessage(Colors.of("&aSuccessfully set value '"+value+"' to global temporary variable '"+name+"'"));
                } catch (Exception e){
                    sender.sendMessage(Colors.of("&cInvalid time format!"));
                }
                return;
            }
        }
        sender.sendMessage(Colors.ofArr(getUsage()));
    }

}
