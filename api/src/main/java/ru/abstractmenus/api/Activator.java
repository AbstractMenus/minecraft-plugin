package ru.abstractmenus.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import ru.abstractmenus.api.inventory.Menu;

/**
 * Menu activator. Activator will be registered as event listener.
 * Do not register it manually, plugin does this automatically
 */
public abstract class Activator implements Listener {

    /**
     * Menu instance
     */
    protected Menu menu;

    /**
     * Set menu to this activator
     * @param menu Target menu
     */
    public void setTargetMenu(Menu menu) {
        this.menu = menu;
    }

    /**
     * Open menu for a player
     * @param ctx Opening context. This can be Bukkit Event or something else
     * @param player Player to open menu
     */
    protected void openMenu(Object ctx, Player player) {
        AbstractMenusProvider.get().openMenu(this, ctx, player, menu);
    }

    /**
     * Get value extractor for this activator. Can be null
     * @return ValueExtractor instance
     */
    public ValueExtractor getValueExtractor() {
        return null;
    }

}
