package ru.abstractmenus.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import ru.abstractmenus.AbstractMenus;

/**
 * Defers menu loading until every plugin has finished {@code onEnable}.
 *
 * <p>Path 1 addons (plugin-as-addons that {@code depend: AbstractMenus} in
 * their {@code plugin.yml}) only run their own {@code onEnable} AFTER AM
 * has finished its own. If AM loaded menus inside its {@code onEnable},
 * any HOCON parse-time validation that depends on a Path 1 contribution
 * (e.g. {@code provider: "playerpoints"} when PlayerPointsAddon is a
 * standalone plugin) would fail because the contribution has not been
 * registered yet.
 *
 * <p>{@link ServerLoadEvent} fires after every plugin's {@code onEnable}
 * has completed - on both server startup ({@code STARTUP} type) and
 * {@code /reload} ({@code RELOAD} type) - so by the time we get here,
 * Path 1 addons have published whatever they were going to publish.
 *
 * <p>The listener unregisters itself after firing once so a hypothetical
 * second event (Bukkit reserves the right) does not double-load menus
 * mid-session.
 */
public final class ServerLoadListener implements Listener {

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        AbstractMenus.instance().loadMenus();
        HandlerList.unregisterAll(this);
    }
}
