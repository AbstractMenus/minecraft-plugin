package ru.abstractmenus.placeholders.hooks;

import org.bukkit.entity.Player;
import ru.abstractmenus.placeholders.PlaceholderHook;
import ru.abstractmenus.services.BungeeManager;

import java.util.Arrays;

public class BungeePlaceholders implements PlaceholderHook {

    @Override
    public String replace(String placeholder, Player player) {
        if(placeholder.startsWith("players_")){
            String[] arr = placeholder.split("_");
            String server = String.join("_", Arrays.copyOfRange(arr, 2, arr.length));
            return String.valueOf(BungeeManager.instance().getOnline(server));
        } else if (placeholder.equals("online")) {
            return String.valueOf(BungeeManager.instance().getOnline());
        }
        return null;
    }

}
