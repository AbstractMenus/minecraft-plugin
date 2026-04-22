package ru.abstractmenus.api;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.variables.VariableManager;

import java.util.Optional;

/**
 * Base plugin interface
 */
public interface AbstractMenusPlugin {

    /**
     * Get plugin instance
     * @return Plugin instance
     */
    Plugin getPlugin();

    /**
     * Get variables manager
     * @return Variables manager instance
     */
    VariableManager getVariableManager();

    /**
     * Reload all menus
     */
    void loadMenus();

    /**
     * Open menu for a player with activator and context
     * @param activator Activator which caused opening. Might be null
     * @param ctx Opening context (object that causes opening)
     * @param player Menu viewer
     * @param menu Menu to open
     */
    void openMenu(Activator activator, Object ctx, Player player, Menu menu);

    /**
     * Open menu for a player
     * @param player Menu viewer
     * @param menu Menu to open
     */
    void openMenu(Player player, Menu menu);

    /**
     * Get opened menu by player who opened this menu
     * @param player Menu viewer
     * @return Found menu in Optional wrapper or Optional.EMPTY otherwise
     */
    Optional<Menu> getOpenedMenu(Player player);

}
