package ru.abstractmenus.api.handler;

import org.bukkit.entity.Player;

/**
 * Permissions handler needs for permission actions and rules
 */
public interface PermissionsHandler {

    /**
     * Give permission to player
     * @param player Required player
     * @param permission Permission to give
     */
    void addPermission(Player player, String permission);

    /**
     * Remove permission from player
     * @param player Required player
     * @param permission Permission to remove
     */
    void removePermission(Player player, String permission);

    /**
     * Check is player has some permission
     * @param player Required player
     * @param permission Permission to check
     * @return true if player has permission or false otherwise
     */
    boolean hasPermission(Player player, String permission);

    /**
     * Add player to specified permissions group
     * @param player Required player
     * @param group Group to add player to it
     */
    void addGroup(Player player, String group);

    /**
     * Remove player from specified permissions group
     * @param player Required player
     * @param group Group to remove player from it
     */
    void removeGroup(Player player, String group);

    /**
     * Check is player exist in specified permissions group
     * @param player Required player
     * @param group Group to check
     * @return true if player member of group or false otherwise
     */
    boolean hasGroup(Player player, String group);

}
