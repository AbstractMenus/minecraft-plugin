package ru.abstractmenus.nms.actionbar;

import org.bukkit.entity.Player;
import ru.abstractmenus.util.NMS;

public abstract class ActionBar extends NMS {

    private static ActionBar bar;

    public static void init() throws ReflectiveOperationException {
        int version = NMS.getMinorVersion();

        if (version == 8) {
            bar = new ActionBar_1_8();
            return;
        }

        // Use deprecated actionbar api until resolve the problem with net.kyori.adventure package replacing
        bar = new ActionBar_1_9();
    }

    public static ActionBar create() {
        return bar;
    }

    public abstract void send(Player player, String message);
}
