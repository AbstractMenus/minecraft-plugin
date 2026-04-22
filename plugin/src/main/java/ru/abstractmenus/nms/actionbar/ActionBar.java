package ru.abstractmenus.nms.actionbar;

import org.bukkit.entity.Player;

public abstract class ActionBar {

    private static volatile ActionBar bar;

    public static void init() {
        bar = new ActionBar_1_9();
    }

    public static ActionBar create() {
        return bar;
    }

    public abstract void send(Player player, String message);
}
