package ru.abstractmenus.commands.am;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.abstractmenus.services.MenuManager;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.text.Colors;

public class CommandOpen extends Command {

    public CommandOpen(){
        setUsage(Colors.of("&e/am open <menu> [player]"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length > 0){
            Menu menu = MenuManager.instance().getMenu(args[0]);

            if(menu == null){
                sender.sendMessage(Colors.of("&cMenu with name '"+args[0]+"' not found."));
                return;
            }

            if(args.length == 2){
                Player player = Bukkit.getServer().getPlayer(args[1]);

                if(player != null && player.isOnline()){
                    MenuManager.instance().openMenu(player, menu);
                    sender.sendMessage(Colors.of("&aOpened menu '"+args[0]+"' to player " + player.getName()));
                    return;
                }
                sender.sendMessage(Colors.of("&cPlayer "+args[1]+" not found"));
                return;
            }

            if(sender instanceof Player){
                MenuManager.instance().openMenu((Player)sender, menu);
                return;
            }
        }

        sender.sendMessage(getUsage());
    }

}
