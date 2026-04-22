package ru.abstractmenus.api.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryHolder;
import ru.abstractmenus.api.Activator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Represents a menu
 */
public interface Menu extends InventoryHolder, Cloneable {

    /**
     * Get activators if this menu
     * @return List of menu activators
     */
    List<Activator> getActivators();

    /**
     * Get activator that caused menu opening. Will be empty, if menu opened without activator
     * @return Activator in Optional wrapper
     */
    Optional<Activator> getActivatedBy();

    /**
     * Get activation context object. This might be event, block, entity, etc
     * @return Activation context object
     */
    Optional<Object> getContext();

    /**
     * Get item in specified slot. This method works with already displayed items
     * @param slot Inventory slot index
     * @return Item object or null if not any items in specified slot
     */
    Item getItem(int slot);

    /**
     * Get all menu items.
     * @return Collection of menu items
     */
    Collection<Item> getItems();

    /**
     * Get menu size
     * @return Size of the menu
     */
    int getSize();

    /**
     * Set size of the menu
     * @param size Required menu size
     */
    void setSize(int size);

    /**
     * Open menu for required player. Do not use this method to open menu. Always use AbstractMenusPlugin instance (wee wiki)
     * @param player Required player
     * @return true if menu opened or false otherwise (for example, if player doesn't match rules)
     */
    boolean open(Player player);

    /**
     * Refresh items in the menu
     * @param player Required player
     */
    void refresh(Player player);

    /**
     * Refresh item in specified inventory slot
     * @param slot Item slot index
     * @param player Menu owner
     */
    void refreshItem(Slot slot, Player player);

    /**
     * Temporary set item in current menu's inventory
     * @param slot Inventory slot index
     * @param item Required item
     * @param player Menu owner
     */
    void setItem(Slot slot, Item item, Player player);

    /**
     * Update this menu. This method uses for auto updates by interval
     * @param player Who opened menu
     */
    void update(Player player);

    /**
     * Close menu for player
     * @param player Who opened menu
     */
    default void close(Player player){
        close(player, true);
    }

    /**
     * Close menu for player
     * @param player Who opened menu
     * @param closeInventory Is inventory must be closed
     */
    void close(Player player, boolean closeInventory);

    /**
     * Process click on specified slot
     * @param slot Clicked slot
     * @param player Player who clicked
     * @param type Click type enum
     */
    void click(int slot, Player player, ClickType type);

    /**
     * Make copy if this menu
     * @return Copy of the menu
     */
    Menu clone();
}
