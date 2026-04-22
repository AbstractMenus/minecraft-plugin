package ru.abstractmenus.api;

import ru.abstractmenus.api.handler.*;

/**
 * Data handlers provider
 */
public final class Handlers {

    private static EconomyHandler economyHandler;
    private static PermissionsHandler permissionsHandler;
    private static LevelHandler levelHandler;
    private static PlaceholderHandler placeholderHandler;
    private static SkinHandler skinHandler;

    private Handlers(){}

    /**
     * Get registered economy handler
     * @return Registered economy handler
     */
    public static EconomyHandler getEconomyHandler() {
        return economyHandler;
    }

    /**
     * Register own economy handler
     * @param handler Economy handler implementation
     */
    public static void setEconomyHandler(EconomyHandler handler) {
        economyHandler = handler;
    }

    /**
     * Get registered permissions handler
     * @return Registered permissions handler
     */
    public static PermissionsHandler getPermissionsHandler() {
        return permissionsHandler;
    }

    /**
     * Register own permissions handler
     * @param handler Permissions handler implementation
     */
    public static void setPermissionsHandler(PermissionsHandler handler) {
        permissionsHandler = handler;
    }

    /**
     * Get registered level handler
     * @return Registered level handler
     */
    public static LevelHandler getLevelHandler() {
        return levelHandler;
    }

    /**
     * Register own level handler
     * @param handler Level handler implementation
     */
    public static void setLevelHandler(LevelHandler handler) {
        levelHandler = handler;
    }

    /**
     * Get registered placeholder handler
     * @return Registered placeholder handler
     */
    public static PlaceholderHandler getPlaceholderHandler() {
        return placeholderHandler;
    }

    /**
     * Register own placeholder handler
     * @param handler Placeholder handler implementation
     */
    public static void setPlaceholderHandler(PlaceholderHandler handler) {
        placeholderHandler = handler;
    }

    /**
     * Get registered skin handler
     * @return Registered skin handler
     */
    public static SkinHandler getSkinHandler() {
        return skinHandler;
    }

    /**
     * Register own skin handler
     * @param handler Skin handler implementation
     */
    public static void setSkinHandler(SkinHandler handler) {
        skinHandler = handler;
    }
}
