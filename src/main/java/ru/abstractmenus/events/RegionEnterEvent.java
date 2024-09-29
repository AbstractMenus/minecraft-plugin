package ru.abstractmenus.events;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;

public class RegionEnterEvent extends RegionEvent {

    public RegionEnterEvent(ProtectedRegion region, Player player){
        super(region, player);
    }

}
