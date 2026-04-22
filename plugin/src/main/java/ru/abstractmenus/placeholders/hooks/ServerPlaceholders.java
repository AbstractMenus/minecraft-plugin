package ru.abstractmenus.placeholders.hooks;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.abstractmenus.placeholders.PlaceholderHook;

public class ServerPlaceholders implements PlaceholderHook {

    private static final String PLAYERS_PREFIX = "players_";

    @Override
    public String replace(String placeholder, Player player) {
        if (placeholder.startsWith(PLAYERS_PREFIX)) {
            String worldName = placeholder.substring(PLAYERS_PREFIX.length());
            World world = Bukkit.getWorld(worldName);
            return world != null ? String.valueOf(world.getPlayers().size()) : "0";
        }

        return switch (placeholder) {
            case "name" -> Bukkit.getServer().getName();
            case "ip" -> Bukkit.getServer().getIp();
            case "port" -> String.valueOf(Bukkit.getServer().getPort());
            case "players" -> String.valueOf(Bukkit.getOnlinePlayers().size());
            case "max_players" -> String.valueOf(Bukkit.getMaxPlayers());
            case "version" -> Bukkit.getVersion();
            default -> null;
        };
    }

}
