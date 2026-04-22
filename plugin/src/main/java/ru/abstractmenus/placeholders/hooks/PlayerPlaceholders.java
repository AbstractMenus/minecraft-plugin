package ru.abstractmenus.placeholders.hooks;

import org.bukkit.entity.Player;
import ru.abstractmenus.placeholders.PlaceholderHook;

public class PlayerPlaceholders implements PlaceholderHook {

    @Override
    public String replace(String placeholder, Player player) {
        if(player != null){
            return switch (placeholder) {
                case "name" -> player.getName();
                case "display_name" -> player.getDisplayName();
                case "level" -> String.valueOf(player.getLevel());
                case "xp" -> String.valueOf(player.getExp());
                case "location" ->
                        player.getLocation().getX() + ", " + player.getLocation().getY() + ", " + player.getLocation().getZ();
                case "uuid" -> player.getUniqueId().toString();
                case "gm" -> player.getGameMode().toString();
                case "world" -> player.getWorld().getName();
                default -> null;
            };
        }

        return null;
    }

}
