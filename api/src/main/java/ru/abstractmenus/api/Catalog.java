package ru.abstractmenus.api;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;

import java.util.Collection;

/**
 * Catalog is an object collection provider
 * Catalog uses for the auto generated menus
 */
public interface Catalog<T> {

    /**
     * Provide collection of objects depend on player and menu state
     * @param player Menu viewer
     * @param menu Current menu
     * @return Collection of objects
     */
    Collection<T> snapshot(Player player, Menu menu);

    /**
     * Get value extractor for values in this catalog
     * @return Value extractor instance
     */
    ValueExtractor extractor();

}
