package ru.abstractmenus.commands;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.api.text.Colors;

public class AbstractMenuCommand extends Command {

    public AbstractMenuCommand(String permission){
        super(permission);
        setUsage(
                Colors.of("&7&l\u25B0\u25B0\u25B0\u25B0\u25B0\u25B0\u25B0\u25B0\u25B0\u25B0\u25B0\u25B0 &eAbstractMenus &7&l\u25B0\u25B0\u25B0\u25B0\u25B0\u25B0\u25B0\u25B0\u25B0\u25B0\u25B0\u25B0"),
                Colors.of("&7/am open <menu_name> &e- Open menu with specific name"),
                Colors.of("&7/am open <menu_name> <player> &e- Open menu with specific name for player"),
                Colors.of("&7/am reload &e- Reload all menu files. Also close all opened menus for player"),
                Colors.of("&7/am serve &e- Turn on/off automatic menu reloading if file changed")
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(getUsage());
    }

}
