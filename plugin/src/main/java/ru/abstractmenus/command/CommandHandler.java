package ru.abstractmenus.command;

import org.bukkit.command.CommandSender;

public interface CommandHandler {

    void handle(CommandSender sender, CommandContext ctx);

}
