package ru.abstractmenus.placeholders.hooks;

import org.bukkit.entity.Player;
import ru.abstractmenus.placeholders.PlaceholderHook;
import ru.abstractmenus.services.HeadAnimManager;

public class HeadAnimPlaceholders implements PlaceholderHook {

    @Override
    public String replace(String placeholder, Player player) {
        String[] arr = placeholder.split(":");
        if(arr.length == 3) {
            return HeadAnimManager.instance().getNextFrame(arr[1], arr[2]);
        }
        return null;
    }
}
