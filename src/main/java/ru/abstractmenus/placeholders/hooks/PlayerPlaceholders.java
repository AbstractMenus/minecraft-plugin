package ru.abstractmenus.placeholders.hooks;

import org.bukkit.entity.Player;
import ru.abstractmenus.placeholders.PlaceholderHook;

public class PlayerPlaceholders implements PlaceholderHook {

    @Override
    public String replace(String placeholder, Player player) {
        if(player != null){
            switch (placeholder){
                default:
                    return null;
                case "name":
                    return player.getName();
                case "display_name":
                    return player.getDisplayName();
                case "level":
                    return String.valueOf(player.getLevel());
                case "xp":
                    return String.valueOf(player.getExp());
                case "location":
                    return player.getLocation().getX() + ", " + player.getLocation().getY() + ", " + player.getLocation().getZ();
                case "uuid":
                    return player.getUniqueId().toString();
                case "gm":
                    return player.getGameMode().toString();
                case "world":
                    return player.getWorld().getName();
            }
        }

        return null;
    }

}
