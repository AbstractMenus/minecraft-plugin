package ru.abstractmenus.commands.am;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import ru.abstractmenus.MainConfig;
import ru.abstractmenus.services.MenuManager;
import ru.abstractmenus.commands.Command;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.text.Colors;

public class CommandOpen extends Command {

    private final MainConfig config;

    public CommandOpen(MainConfig config) {
        this.config = config;
        setUsage(Colors.of("&e/am open <menu> [player]"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(getUsage());
            return;
        }

        Menu menu = MenuManager.instance().getMenu(args[0]);
        if (menu == null) {
            sender.sendMessage(Colors.of("&cMenu with name '" + args[0] + "' not found."));
            return;
        }

        if (args.length == 2) {
            Player player = Bukkit.getServer().getPlayer(args[1]);
            if (player == null || !player.isOnline()) {
                sender.sendMessage(Colors.of("&cPlayer " + args[1] + " not found"));
                return;
            }

            MenuManager.instance().openMenu(player, menu);
            if (config.isLogOpenMenus()) {
                sender.sendMessage(Colors.of(String.format("Opened menu '%s' to player %s", args[0], player.getName())));
            }
            return;
        }

        if (sender instanceof Player player) {
            MenuManager.instance().openMenu(player, menu);
            return;
        }

        sender.sendMessage(getUsage());
    }
}
