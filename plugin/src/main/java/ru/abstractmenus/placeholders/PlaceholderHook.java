package ru.abstractmenus.placeholders;

import org.bukkit.entity.Player;

public interface PlaceholderHook {

    String replace(String placeholder, Player player);

}
