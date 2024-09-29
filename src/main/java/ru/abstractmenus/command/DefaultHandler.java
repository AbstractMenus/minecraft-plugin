package ru.abstractmenus.command;

import org.bukkit.command.CommandSender;

public class DefaultHandler implements CommandHandler {

    public static final DefaultHandler INSTANCE = new DefaultHandler();

    @Override
    public void handle(CommandSender sender, CommandContext ctx) {

    }

}
