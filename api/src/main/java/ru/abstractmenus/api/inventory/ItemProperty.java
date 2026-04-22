package ru.abstractmenus.api.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Represent any item property
 */
public interface ItemProperty {

    /**
     * Is this property replaces item type of given item.
     *
     * Properties which replaces material always will be assigned first
     * to create valid item meta for ItemStack
     *
     * @return true if this property replaces material or false otherwise
     */
    boolean canReplaceMaterial();

    /**
     * Is this property allows to assign modified meta after exiting from {@link ItemProperty#apply} method.
     *
     * Return true if you make simple properties which modify only ItemMeta
     * and doesn't touches ItemStack to assign it manually.
     *
     * If you set it for true and set item meta manually
     * inside {@link ItemProperty#apply} method, then all changes won't be saved.
     * @return true if allow or false otherwise
     */
    boolean isApplyMeta();

    /**
     * Apply property to ItemStack.
     * @param item Source ItemStack.
     * @param meta Current meta of this item.
     * @param player Player for who this item builds.
     * @param menu Menu which cause item building.
     */
    void apply(ItemStack item, ItemMeta meta, Player player, Menu menu);

}
