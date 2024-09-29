package ru.abstractmenus.commands.am;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.services.HeadAnimManager;
import ru.abstractmenus.services.MenuManager;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.api.Logger;

public class CommandReload extends Command {

    public CommandReload() {
        setUsage(Colors.of("&e/am reload"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            MenuManager.instance().loadMenus();
            HeadAnimManager.instance().loadAnimations();
            Logger.info("Plugin reloaded!");

            if(sender instanceof Player) sender.sendMessage(Colors.of("&a&l[AbstractMenus] plugin reloaded!"));
        } catch (Exception e){
            Logger.severe("Error while reloading plugin: " + e.getMessage());
        }
    }

}
