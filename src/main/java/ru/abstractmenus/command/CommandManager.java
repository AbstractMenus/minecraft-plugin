package ru.abstractmenus.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import ru.abstractmenus.command.bukkit.CommandWrapper;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public final class CommandManager {

    private final Plugin plugin;
    private final Map<String, Command> commands = new HashMap<>();
    private SimpleCommandMap commandMap;

    public CommandManager(Plugin plugin) {
        this.plugin = plugin;

        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> craftServerClass = Class.forName(String.format("org.bukkit.craftbukkit.%s.CraftServer", version));
            Method getCommandMap = craftServerClass.getDeclaredMethod("getCommandMap");
            commandMap = (SimpleCommandMap) getCommandMap.invoke(Bukkit.getServer());
        } catch (Exception e) {
            commandMap = (SimpleCommandMap) Bukkit.getCommandMap();
        }
    }

    public Command get(String name) {
        return commands.get(name.toLowerCase());
    }

    public void register(Command cmd) {
        CommandWrapper wrapper = new CommandWrapper(cmd.getName(),
                "AbstractMenus generated command",
                cmd.help(),
                cmd.getAliases(),
                this);

        commandMap.register(cmd.getName(), plugin.getDescription().getName(), wrapper);
        addCommand(cmd);
    }

    public void unregister(Command cmd) {
        if (cmd == null) return;
        remCommand(cmd);
        org.bukkit.command.Command bcmd = commandMap.getCommand(cmd.getName());
        if (bcmd != null)
            bcmd.unregister(commandMap);
    }

    private void addCommand(Command cmd) {
        commands.put(cmd.getName().toLowerCase(), cmd);

        for (String alias : cmd.getAliases()) {
            commands.put(alias.toLowerCase(), cmd);
        }
    }

    private void remCommand(Command cmd) {
        commands.remove(cmd.getName().toLowerCase());
        cmd.getAliases().forEach(commands::remove);
    }

    public void process(CommandSender sender, String label, String[] argsArr) {
        Command cmd = get(label);

        if (cmd != null) {
            List<String> args = Arrays.asList(argsArr);
            cmd.execute(sender, args);
        }
    }

    public List<String> complete(CommandSender sender, String label, String[] argsArr) {
        Command cmd = get(label);

        if (cmd != null) {
            List<String> args = Arrays.asList(argsArr);
            List<String> suggestions = cmd.suggest(sender, args);

            if (argsArr.length > 0 && suggestions != null) {
                String last = argsArr[argsArr.length-1];

                return suggestions.stream()
                        .filter(val -> val.startsWith(last))
                        .collect(Collectors.toList());
            }
        }

        return null;
    }

}
