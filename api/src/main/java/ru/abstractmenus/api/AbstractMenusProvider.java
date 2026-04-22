package ru.abstractmenus.api;

/**
 * Plugin provider. Provides access to plugin instance
 */
public final class AbstractMenusProvider {

    private static AbstractMenusPlugin plugin;

    private AbstractMenusProvider(){}

    /**
     * Initialize plugin instance
     * @param plug Plugin instance
     */
    public static void init(AbstractMenusPlugin plug){
        plugin = plug;
    }

    /**
     * Get AbstractMenus plugin instance
     * @return Plugin instance
     */
    public static AbstractMenusPlugin get(){
        return plugin;
    }

}
