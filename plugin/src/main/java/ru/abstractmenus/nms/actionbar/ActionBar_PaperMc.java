package ru.abstractmenus.nms.actionbar;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class ActionBar_PaperMc extends ActionBar {

    @Override
    public void send(Player player, String message) {
        player.sendActionBar(Component.text(message));
    }
}
