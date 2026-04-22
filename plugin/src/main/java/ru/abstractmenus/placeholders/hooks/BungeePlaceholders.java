package ru.abstractmenus.placeholders.hooks;

import org.bukkit.entity.Player;
import ru.abstractmenus.placeholders.PlaceholderHook;
import ru.abstractmenus.services.BungeeManager;

public class BungeePlaceholders implements PlaceholderHook {

    private static final String PLAYERS_PREFIX = "players_";

    @Override
    public String replace(String placeholder, Player player) {
        if (placeholder.startsWith(PLAYERS_PREFIX)) {
            String server = placeholder.substring(PLAYERS_PREFIX.length());
            return String.valueOf(BungeeManager.instance().getOnline(server));
        } else if (placeholder.equals("online")) {
            return String.valueOf(BungeeManager.instance().getOnline());
        }
        return null;
    }

}
