package ru.abstractmenus.placeholders.hooks;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.abstractmenus.placeholders.PlaceholderHook;

import java.util.Arrays;

public class ServerPlaceholders implements PlaceholderHook {

    @Override
    public String replace(String placeholder, Player player) {
        if(placeholder.startsWith("players_")) {
            String[] arr = placeholder.split("_");
            String worldName = String.join("_", Arrays.copyOfRange(arr, 2, arr.length));
            World world = Bukkit.getWorld(worldName);
            return world != null ? String.valueOf(world.getPlayers().size()) : "0";
        }

        switch (placeholder){
            default:
                return null;
            case "name":
                return Bukkit.getServer().getName();
            case "ip":
                return Bukkit.getServer().getIp();
            case "port":
                return String.valueOf(Bukkit.getServer().getPort());
            case "players":
                return String.valueOf(Bukkit.getOnlinePlayers().size());
            case "max_players":
                return String.valueOf(Bukkit.getMaxPlayers());
            case "version":
                return Bukkit.getVersion();
        }
    }

}
