package ru.abstractmenus.api.handler;

import org.bukkit.entity.Player;

/**
 * Skins handler needs for skin actions and rules
 */
public interface SkinHandler {

    /**
     * Set player's skin by provided texture and signature
     * @param player Required player
     * @param texture Skin texture
     * @param signature Skin signature
     */
    void setSkin(Player player, String texture, String signature);

    /**
     * Reset player's skin to default
     * @param player Required player
     */
    void resetSkin(Player player);

}
