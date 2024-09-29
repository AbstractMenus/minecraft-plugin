package ru.abstractmenus.commands.varp;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.api.variables.Var;
import ru.abstractmenus.variables.VariableManagerImpl;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.util.TimeUtil;

public class VarpSet extends Command {

    public VarpSet() {
        setUsage("/varp set <player> <name> <value> - Set global variable",
                "/varp set <player> <name> <value> <replace> - Set global variable with replace protection",
                "/varp set <player> <name> <value> <time> - Set global temporary variable",
                "/varp set <player> <name> <value> <time> <replace> - Set global temporary variable with replace protection");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length >= 2){
            String player = args[0];
            String name = args[1];
            String value = args[2];

            Var var = VariableManagerImpl.instance().createBuilder()
                    .name(name)
                    .value(value)
                    .build();

            if(args.length == 3){
                // just set var
                VariableManagerImpl.instance().savePersonal(player, var);
                sender.sendMessage(Colors.of("&aSuccessfully set value '"+value+"' to personal "+player+" variable '"+name+"'"));
                return;
            }
            if(args.length == 4){
                if(args[3].equals("true") || args[3].equals("false")){
                    // set replace var
                    boolean replace = Boolean.parseBoolean(args[3]);
                    VariableManagerImpl.instance().savePersonal(player, var, replace);
                    sender.sendMessage(Colors.of("&aSuccessfully set value '"+value+"' to personal "+player+" variable '"+name+"' (replace="+replace+")"));
                    return;
                }
                // set time var
                try{
                    long time = TimeUtil.parseTime(args[3]);
                    var = var.toBuilder()
                            .expiry(System.currentTimeMillis() + time)
                            .build();
                    VariableManagerImpl.instance().savePersonal(player, var);
                    sender.sendMessage(Colors.of("&aSuccessfully set value '"+value+"' to personal "+player+" temporary variable '"+name+"'"));
                } catch (Exception e){
                    sender.sendMessage(Colors.of("&cInvalid time format!"));
                }
                return;
            }
            if(args.length == 5){
                // set time replace var
                try {
                    long time = TimeUtil.parseTime(args[2]);
                    boolean replace = Boolean.parseBoolean(args[3]);
                    var = var.toBuilder()
                            .expiry(System.currentTimeMillis() + time)
                            .build();
                    VariableManagerImpl.instance().savePersonal(player, var, replace);
                    sender.sendMessage(Colors.of("&aSuccessfully set value '"+value+"' to personal "+player+" temporary variable '"+name+"'"));
                } catch (Exception e){
                    sender.sendMessage(Colors.of("&cInvalid time format!"));
                }
                return;
            }
        }
        sender.sendMessage(Colors.ofArr(getUsage()));
    }

}
