package ru.abstractmenus.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public abstract class Command implements CommandExecutor {

    private String permission;
    private String[] usage;
    private final Map<String, Command> subCommands = new HashMap<>();

    public Command(){ }

    public Command(String permission){
        this.permission = permission;
    }

    public String[] getUsage(){
        return usage;
    }

    public String getPermission(){
        return permission;
    }

    public Command setPermission(String permission){
        this.permission = permission;
        return this;
    }

    public Command addSub(String arg, Command command){
        subCommands.put(arg, command);
        return this;
    }

    public Command getSub(String arg){
        return subCommands.get(arg);
    }

    public Command setUsage(String... usage){
        this.usage = usage;
        return this;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if(!checkPermission(sender, this)) return false;

        if(args.length > 0){
            Stack<Command> stack = new Stack<>();
            Command sub;

            for (String arg : args){
                if(stack.isEmpty()){
                    sub = getSub(arg);
                    if(sub != null) {
                        stack.push(sub);
                        continue;
                    }
                    break;
                }

                sub = stack.peek().getSub(arg);
                if(sub != null) {
                    stack.push(sub);
                    continue;
                }
                break;
            }

            if(!stack.isEmpty()){
                Command command = stack.pop();
                if(!checkPermission(sender, command)) return false;
                command.execute(sender, Arrays.copyOfRange(args, stack.size()+1, args.length));
                return true;
            }

            if(usage != null){
                sender.sendMessage(usage);
            }
            return false;
        }

        execute(sender, args);
        return true;
    }

    private boolean checkPermission(CommandSender sender, Command command){
        if(command.getPermission() != null){
            return sender.hasPermission(command.getPermission());
        }
        return true;
    }

    public abstract void execute(CommandSender sender, String[] args);

}
