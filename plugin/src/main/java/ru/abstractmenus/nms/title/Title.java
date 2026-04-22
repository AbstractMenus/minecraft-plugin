package ru.abstractmenus.nms.title;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
@AllArgsConstructor
public class Title {

    private static volatile TitleSender sender;
    private String title;
    private String subtitle;
    private int fadeIn;
    private int fadeOut;
    private int stay;

    public void send(Player player) {
        sender.send(player, this);
    }

    public static void init() {
        sender = new SenderModern();
    }
}