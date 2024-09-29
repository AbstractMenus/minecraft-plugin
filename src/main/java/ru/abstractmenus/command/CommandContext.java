package ru.abstractmenus.command;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CommandContext {

    private final Command command;
    private final Map<String, Object> args = new HashMap<>();

    public CommandContext(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

    public Collection<Object> values() {
        return args.values();
    }

    public int args() {
        return args.size();
    }

    public Object get(String key) {
        return args.get(key);
    }

    public String getString(String key) {
        Object val = get(key);
        if (val instanceof Player)
            return ((Player)val).getName();
        return val.toString();
    }

    public void add(String key, Object value) {
        args.put(key, value);
    }

}
