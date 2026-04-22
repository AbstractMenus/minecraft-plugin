package ru.abstractmenus.api;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;

/**
 * Represents the rule that the player must meets in order to proceed.
 */
@FunctionalInterface
public interface Rule {

    /**
     * Check if the player meets the rule
     * @param player Player to check
     * @param menu Menu in which this rule works
     * @param clickedItem An item which player might click and initiate this checking. Might be null
     * @return true if player meets the rule or false of not
     */
    boolean check(Player player, Menu menu, Item clickedItem);

}
