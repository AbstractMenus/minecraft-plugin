package ru.abstractmenus.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.abstractmenus.services.MenuManager;
import ru.abstractmenus.services.ProfileStorage;
import ru.abstractmenus.util.bukkit.Skulls;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Drop any cached skin so the next %player_*% / PropSkullOwner render
        // picks up a possibly changed skin.
        Skulls.invalidatePlayerSkull(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Drop any tracked menu before the entity is gone. InventoryListener
        // also closes via runForEntityLater(player, ..., 3L), but on Folia
        // that task is silently dropped if the player quits inside the 3-tick
        // window - leaving openedMenus with a stale entry. Removing here
        // guarantees cleanup regardless of timing.
        MenuManager.instance().removePlayerMenu(event.getPlayer());
        MenuManager.instance().getAndRemoveInputAction(event.getPlayer());
        ProfileStorage storage = ProfileStorage.instance();
        if (storage != null) {
            storage.remove(event.getPlayer());
        }
    }

}
