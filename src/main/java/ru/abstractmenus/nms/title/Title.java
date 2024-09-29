package ru.abstractmenus.nms.title;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import ru.abstractmenus.util.NMS;

@Getter
@Setter
@AllArgsConstructor
//todo need refactoring
public class Title {

    private static TitleSender sender;
    private String title;
    private String subtitle;
    private int fadeIn;
    private int fadeOut;
    private int stay;

    public void send(Player player) {
        sender.send(player, this);
    }

    public static void init() {
        if (NMS.getMinorVersion() <= 9) {
            // For 1.9 and older
            sender = new SenderLegacy();
        } else {
            sender = new SenderModern();
        }
    }
}