package ru.abstractmenus.commands.am;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.commands.Command;

public class CommandPluginVersion extends Command {

    public CommandPluginVersion() {
        setUsage(Colors.of("&e/am version"));
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void execute(CommandSender sender, String[] args) {
        String pluginVersion = AbstractMenus.instance().getPlugin().getPluginMeta().getVersion();
        sender.sendRichMessage("<b><green>[AbstractMenus] version: " + pluginVersion);
    }

}
