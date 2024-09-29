package ru.abstractmenus.commands.am;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.services.HeadAnimManager;
import ru.abstractmenus.services.MenuManager;

public class CommandServe extends Command {

    public CommandServe() {
        setUsage(Colors.of("&e/am serve"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            boolean result = MenuManager.instance().serve();

            if (result) {
                sender.sendMessage(Colors.of("&a&l[AbstractMenus] start listening for changes"));
            } else {
                sender.sendMessage(Colors.of("&a&l[AbstractMenus] stop listening for changes"));
            }
        } catch (Exception e) {
            Logger.severe("Error while turning serving of files: " + e.getMessage());
        }
    }

}
