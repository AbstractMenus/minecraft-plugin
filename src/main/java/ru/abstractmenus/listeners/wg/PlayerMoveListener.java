package ru.abstractmenus.listeners.wg;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.abstractmenus.util.RegionUtils;
import ru.abstractmenus.events.RegionEnterEvent;
import ru.abstractmenus.events.RegionLeaveEvent;

import java.util.Map;
import java.util.TreeMap;

public class PlayerMoveListener implements Listener {

    private final Map<String, ProtectedRegion> joinedRegions = new TreeMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        Iterable<ProtectedRegion> regions = RegionUtils.getRegions(event.getPlayer().getWorld());

        if(regions != null){
            for(ProtectedRegion region : regions){
                Location from = event.getFrom();
                Location to = event.getTo();

                if(to != null){
                    if(region.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ())){
                        if(!region.equals(joinedRegions.get(event.getPlayer().getName()))){
                            Bukkit.getServer().getPluginManager().callEvent(new RegionEnterEvent(region, event.getPlayer()));
                            joinedRegions.put(event.getPlayer().getName(), region);
                        }
                        continue;
                    }
                }

                if(region.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ())){
                    if(region.equals(joinedRegions.get(event.getPlayer().getName()))){
                        Bukkit.getServer().getPluginManager().callEvent(new RegionLeaveEvent(region, event.getPlayer()));
                        joinedRegions.remove(event.getPlayer().getName());
                    }
                    return;
                }
            }
        }
    }

}
