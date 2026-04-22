package ru.abstractmenus.nms.title;

import org.bukkit.entity.Player;

/**
 * Title sender for 1.10+
 */
public class SenderModern implements TitleSender {

    @Override
    public void send(Player player, Title title) {
        player.sendTitle(title.getTitle(), title.getSubtitle(),
                title.getFadeIn(),
                title.getStay(),
                title.getFadeOut());
    }
}
