package ru.abstractmenus.api.handler;

import org.bukkit.entity.Player;

/**
 * Economy handler needs for economy actions and rules
 */
public interface EconomyHandler {

    /**
     * Check is player has balance
     * @param player Player to check
     * @param balance Required balance
     * @return true if player has required amount on balance
     */
    boolean hasBalance(Player player, double balance);

    /**
     * Withdraw some amount from player's balance
     * @param player Required player
     * @param amount Required amount
     */
    void takeBalance(Player player, double amount);

    /**
     * Give some amount to player's balance
     * @param player Required player
     * @param amount Required amount
     */
    void giveBalance(Player player, double amount);

}
