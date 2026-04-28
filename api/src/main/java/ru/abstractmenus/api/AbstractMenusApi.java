package ru.abstractmenus.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.variables.VariableManager;
import ru.abstractmenus.hocon.api.serialize.NodeSerializers;

import java.util.Optional;

/**
 * Runtime entry point for AbstractMenus. Exposes the type registries, the
 * shared HOCON {@link NodeSerializers}, menu lifecycle operations, and the
 * variable manager.
 *
 * <p>Obtain the running instance via {@link #get()}:
 *
 * <pre>{@code
 * AbstractMenusApi api = AbstractMenusApi.get();
 * api.actions().register("myAction", MyAction.class, new MyAction.Serializer(), this);
 * }</pre>
 *
 * <p>The implementation is registered against Bukkit's {@code ServicesManager}
 * during the plugin's {@code onEnable()}. Do not call {@link #get()} before
 * AbstractMenus has enabled &mdash; you will get {@code null}. For
 * plugin-as-addon (Path 1) usage, call from your own {@code onEnable()} with
 * {@code depend: [AbstractMenus]} in {@code plugin.yml} to guarantee ordering.
 *
 * @see MenuExtension
 * @see TypeRegistry
 */
public interface AbstractMenusApi {

    /**
     * Look up the running API instance from Bukkit's ServicesManager.
     *
     * @return the running API, or {@code null} if AbstractMenus has not
     *         enabled yet
     */
    static AbstractMenusApi get() {
        return Bukkit.getServicesManager().load(AbstractMenusApi.class);
    }

    // ---- Registries -----------------------------------------------------

    /** @return the action registry */
    TypeRegistry<Action> actions();

    /** @return the rule registry */
    TypeRegistry<Rule> rules();

    /** @return the activator registry */
    TypeRegistry<Activator> activators();

    /** @return the item-property registry */
    TypeRegistry<ItemProperty> itemProperties();

    /** @return the catalog registry */
    TypeRegistry<Catalog<?>> catalogs();

    /**
     * Handler-provider registry (economy, permissions, levels, placeholders,
     * skins). Replaces the old static {@code Handlers} facade.
     *
     * @return the provider registry
     */
    ProviderRegistry providers();

    /**
     * Shared HOCON serializer collection. Type registrations add to this
     * automatically; consumers rarely need to access it directly.
     *
     * @return the shared serializer collection
     */
    NodeSerializers serializers();

    // ---- Subsystems -----------------------------------------------------

    /** @return the plugin's variable manager */
    VariableManager variables();

    /**
     * The underlying Bukkit {@link Plugin} instance (the AbstractMenus
     * plugin itself). Exposed for scheduler access and listener registration;
     * prefer the high-level methods on this interface when possible.
     *
     * @return the plugin
     */
    Plugin getPlugin();

    // ---- Menu lifecycle (migrated from AbstractMenusPlugin) -------------

    /**
     * Reload all menus from {@code plugins/AbstractMenus/menus/}. Called by
     * {@code /am reload}.
     */
    void loadMenus();

    /**
     * Open a menu for a player with a triggering activator and context.
     *
     * @param activator the activator that triggered opening (may be
     *                  {@code null} for programmatic opens)
     * @param ctx       opening context (source entity/block/etc., may be
     *                  {@code null})
     * @param player    the viewer
     * @param menu      the menu to open
     */
    void openMenu(Activator activator, Object ctx, Player player, Menu menu);

    /**
     * Open a menu for a player with no activator or context.
     *
     * @param player the viewer
     * @param menu   the menu to open
     */
    void openMenu(Player player, Menu menu);

    /**
     * Get the menu currently open for {@code player}.
     *
     * @param player the viewer
     * @return the open menu, or {@link Optional#empty()} if the player has no
     *         AbstractMenus menu open
     */
    Optional<Menu> getOpenedMenu(Player player);

    // ---- Meta -----------------------------------------------------------

    /**
     * API version string, taken from the plugin's {@code version}. Purely
     * diagnostic &mdash; logged on startup and surfaced in
     * {@code /am addons list}.
     *
     * @return the API version (e.g. {@code "2.0.0-alpha.2"})
     */
    String apiVersion();
}
