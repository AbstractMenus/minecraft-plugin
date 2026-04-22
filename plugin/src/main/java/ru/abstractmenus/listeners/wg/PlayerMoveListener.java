package ru.abstractmenus.listeners.wg;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.abstractmenus.events.RegionEnterEvent;
import ru.abstractmenus.events.RegionLeaveEvent;
import ru.abstractmenus.util.RegionUtils;

import java.util.Map;
import java.util.TreeMap;

public class PlayerMoveListener implements Listener {

    private final Map<String, ProtectedRegion> joinedRegions = new TreeMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Iterable<ProtectedRegion> regions = RegionUtils.getRegions(event.getPlayer().getWorld());

        if (regions == null) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        Player player = event.getPlayer();
        String playerName = player.getName();
        ProtectedRegion joinedRegion = joinedRegions.get(playerName);

        for (ProtectedRegion region : regions) {
            boolean isEntering = region.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ());
            boolean isLeaving = region.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ());

            if (isEntering && !region.equals(joinedRegion)) {
                Bukkit.getServer().getPluginManager().callEvent(new RegionEnterEvent(region, player));
                joinedRegions.put(playerName, region);
                continue;
            }

            if (isLeaving && region.equals(joinedRegion)) {
                Bukkit.getServer().getPluginManager().callEvent(new RegionLeaveEvent(region, player));
                joinedRegions.remove(playerName);
                return;
            }
        }
    }


}
