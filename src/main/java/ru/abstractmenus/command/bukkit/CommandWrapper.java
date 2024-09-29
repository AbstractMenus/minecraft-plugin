package ru.abstractmenus.command.bukkit;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import ru.abstractmenus.command.CommandManager;

import java.util.Collections;
import java.util.List;

public final class CommandWrapper extends BukkitCommand {

    private final CommandManager manager;

    public CommandWrapper(String name, String description, String usageMessage, List<String> aliases, CommandManager manager) {
        super(name, description, usageMessage, aliases);
        this.manager = manager;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        manager.process(sender, label, args);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        List<String> suggestions = manager.complete(sender, alias, args);
        return suggestions != null ? suggestions : Collections.emptyList();
    }
}