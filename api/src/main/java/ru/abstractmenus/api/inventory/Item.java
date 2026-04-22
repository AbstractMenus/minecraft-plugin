package ru.abstractmenus.api.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Represent buildable ItemStack
 */
public interface Item extends Cloneable {

    /**
     * Get properties if the item
     * @return List of item properties
     */
    Map<String, ItemProperty> getProperties();

    /**
     * Add some property for this item
     * @param key Property key
     * @param property Item property
     */
    void addProperty(String key, ItemProperty property);

    /**
     * Set new or replace all properties for this item
     * @param properties Properties map
     */
    void setProperties(Map<String, ItemProperty> properties);

    /**
     * Remove property from item
     * @param key Key (name) of the property. This key you specify in item block in menu file
     * @return Removed property or null if property with specified key is not assigned to item
     */
    ItemProperty removeProperty(String key);

    /**
     * Check is this item similar to som ItemStack
     * @param item Bukkit ItemStack
     * @param player Player to replace placeholders
     * @return true if this item similar to provided or false otherwise
     */
    boolean isSimilar(ItemStack item, Player player);

    /**
     * Build ItemStack of this item
     * @param player Player to correct replace all placeholders
     * @param menu Menu in which this item exists. Might be null
     * @return Built ItemStack object
     */
    ItemStack build(Player player, Menu menu);

    /**
     * Clone this item
     * @return Cloned item
     */
    Item clone();
}
