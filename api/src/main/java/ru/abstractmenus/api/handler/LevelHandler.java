package ru.abstractmenus.api.handler;

import org.bukkit.entity.Player;

/**
 * Level handler needs for level actions and rules
 */
public interface LevelHandler {

    /**
     * Get player's exp
     * @param player Required player
     * @return Count of player's exp
     */
    int getXp(Player player);

    /**
     * Give some exp for player
     * @param player Required player
     * @param xp Number of experience
     */
    void giveXp(Player player, int xp);

    /**
     * Take some exp from player
     * @param player Required player
     * @param xp Number of experience
     */
    void takeXp(Player player, int xp);

    /**
     * Get player's level
     * @param player Required player
     * @return Number of player's level
     */
    int getLevel(Player player);

    /**
     * Give some level for player
     * @param player Required player
     * @param level Number of levels to add
     */
    void giveLevel(Player player, int level);

    /**
     * Take some level from player
     * @param player Required player
     * @param level Number of levels to withdraw
     */
    void takeLevel(Player player, int level);

}
