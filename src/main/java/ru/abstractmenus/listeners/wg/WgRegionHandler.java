package ru.abstractmenus.listeners.wg;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.abstractmenus.events.RegionEnterEvent;
import ru.abstractmenus.events.RegionLeaveEvent;

import java.util.Set;

public class WgRegionHandler extends Handler {

    protected WgRegionHandler(Session session) {
        super(session);
    }

    @Override
    public boolean testMoveTo(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, MoveType moveType) {
        return true;
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer localPlayer, Location from, Location to, ApplicableRegionSet toSet,
                                   Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType)
    {
        Player player = BukkitAdapter.adapt(localPlayer);

        if (player != null) {
            if (!entered.isEmpty()) {
                entered.forEach(region -> callEnter(region, player));
            }

            if (!exited.isEmpty()) {
                exited.forEach(region -> callExit(region, player));
            }
        }

        return true;
    }

    private void callEnter(ProtectedRegion region, Player player) {
        Bukkit.getServer().getPluginManager().callEvent(new RegionEnterEvent(region, player));
    }

    private void callExit(ProtectedRegion region, Player player) {
        Bukkit.getServer().getPluginManager().callEvent(new RegionLeaveEvent(region, player));
    }

    public static class Factory extends Handler.Factory<WgRegionHandler> {

        @Override
        public WgRegionHandler create(Session session) {
            return new WgRegionHandler(session);
        }
    }
}
