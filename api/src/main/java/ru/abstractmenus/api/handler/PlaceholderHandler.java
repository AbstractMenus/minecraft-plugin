package ru.abstractmenus.api.handler;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Placeholder handler needs for placeholder replacing in items, actions, rules, etc.
 */
public interface PlaceholderHandler {

    /**
     * Replace only given placeholder
     * @param player Context player
     * @param placeholder Placeholder key without '%' chars
     * @return Replaced value
     */
    String replacePlaceholder(Player player, String placeholder);

    /**
     * Replace placeholders in single string
     * @param player Player who opened menu
     * @param str String to replace
     * @return String with replaced placeholders
     */
    String replace(Player player, String str);

    /**
     * Replace placeholders in strings list
     * @param player Player who opened menu
     * @param list Strings list to replace
     * @return String with replaced placeholders
     */
    List<String> replace(Player player, List<String> list);

    /**
     * Register placeholders
     */
    void registerAll();

}
