package ru.abstractmenus.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.hocon.api.serialize.NodeSerializers;

/**
 * Serializers provider. All data types for menus registering here
 */
public final class Types {

    private static final NodeSerializers SERIALIZERS = NodeSerializers.defaults();
    private static final BiMap<String, Class<? extends Action>> ACTION_TYPES = HashBiMap.create();
    private static final BiMap<String, Class<? extends Rule>> RULE_TYPES = HashBiMap.create();
    private static final BiMap<String, Class<? extends Activator>> ACTIVATOR_TYPES = HashBiMap.create();
    private static final BiMap<String, Class<? extends ItemProperty>> ITEM_PROPERTIES_TYPES = HashBiMap.create();
    private static final BiMap<String, Class<? extends Catalog<?>>> CATALOG_TYPES = HashBiMap.create();

    private Types() { }

    /**
     * Global serializers collection
     * @return Serializers collection
     */
    public static NodeSerializers serializers() {
        return SERIALIZERS;
    }

    /**
     * Get action name by type
     * @param type Action type
     * @return Action name or null if not found
     */
    public static String getActionName(Class<? extends Action> type) {
        return ACTION_TYPES.inverse().get(type);
    }

    /**
     * Get rule name by type
     * @param type Rule type
     * @return Rule name or null if not found
     */
    public static String getRuleName(Class<? extends Rule> type) {
        return RULE_TYPES.inverse().get(type);
    }

    /**
     * Get activator name by type
     * @param type Activator type
     * @return Activator name or null if not found
     */
    public static String getActivatorName(Class<? extends Activator> type) {
        return ACTIVATOR_TYPES.inverse().get(type);
    }

    /**
     * Get item property name by type
     * @param type Item property type
     * @return Item property name or null if not found
     */
    public static String getItemPropertyName(Class<? extends ItemProperty> type) {
        return ITEM_PROPERTIES_TYPES.inverse().get(type);
    }

    /**
     * Get catalog name by type
     * @param type Catalog type
     * @return Catalog name or null if not found
     */
    public static String getCatalogName(Class<? extends Catalog> type) {
        return CATALOG_TYPES.inverse().get(type);
    }

    /**
     * Register new action
     * @param key Key of the action. This key uses in menu file
     * @param token Type of the action
     * @param serializer Serializer of the action
     * @param <T> Type of the action
     */
    public static <T extends Action> void registerAction(String key, Class<T> token, NodeSerializer<T> serializer){
        serializers().register(token, serializer);
        ACTION_TYPES.put(key.toLowerCase(), token);
    }

    /**
     * Get token to deserialize action from menu file
     * @param key Action key
     * @return Found TypeToken of the registered action of null if token not found
     */
    public static Class<? extends Action> getActionType(String key){
        return ACTION_TYPES.get(key.toLowerCase());
    }

    /**
     * Register new rule
     * @param key Key of the rule. This key uses in menu file
     * @param token Type of the rule
     * @param serializer Serializer of the rule
     * @param <T> Type of the rule
     */
    public static <T extends Rule> void registerRule(String key, Class<T> token, NodeSerializer<T> serializer){
        serializers().register(token, serializer);
        RULE_TYPES.put(key.toLowerCase(), token);
    }

    /**
     * Get token to deserialize rule from menu file
     * @param key Rule key
     * @return Found TypeToken of the registered rule of null if token not found
     */
    public static Class<? extends Rule> getRuleType(String key){
        return RULE_TYPES.get(key.toLowerCase());
    }

    /**
     * Register new menu activator
     * @param key Key of the activator. This key uses in menu file
     * @param token Type of the activator
     * @param serializer Serializer of the activator
     * @param <T> Type of the activator
     */
    public static <T extends Activator> void registerActivator(String key, Class<T> token, NodeSerializer<T> serializer){
        serializers().register(token, serializer);
        ACTIVATOR_TYPES.put(key.toLowerCase(), token);
    }

    /**
     * Get token to deserialize activator from menu file
     * @param key Activator key
     * @return Found TypeToken of the registered activator of null if token not found
     */
    public static Class<? extends Activator> getActivator(String key){
        return ACTIVATOR_TYPES.get(key.toLowerCase());
    }

    /**
     * Register new item property
     * @param key Key of the property. This key uses in menu file
     * @param token Type of the property
     * @param serializer Serializer of the property
     * @param <T> Type of the property
     */
    public static <T extends ItemProperty> void registerItemProperty(String key, Class<T> token, NodeSerializer<T> serializer){
        serializers().register(token, serializer);
        ITEM_PROPERTIES_TYPES.put(key.toLowerCase(), token);
    }

    /**
     * Get token to deserialize item property from menu file
     * @param key Item property key
     * @return Found TypeToken of the registered item property of null if token not found
     */
    public static Class<? extends ItemProperty> getItemPropertyType(String key){
        return ITEM_PROPERTIES_TYPES.get(key.toLowerCase());
    }

    /**
     * Register new catalog
     * @param key Key of the catalog. This key uses in menu file
     * @param token Type of the catalog
     * @param serializer Serializer of the catalog
     * @param <T> Type of the catalog
     */
    public static <T extends Catalog<?>> void registerCatalog(String key, Class<T> token, NodeSerializer<T> serializer){
        serializers().register(token, serializer);
        CATALOG_TYPES.put(key.toLowerCase(), token);
    }

    /**
     * Get token to deserialize catalog from menu file
     * @param key Catalog key
     * @return Found TypeToken of the registered catalog of null if token not found
     */
    public static Class<? extends Catalog<?>> getCatalogType(String key){
        return CATALOG_TYPES.get(key.toLowerCase());
    }
}
