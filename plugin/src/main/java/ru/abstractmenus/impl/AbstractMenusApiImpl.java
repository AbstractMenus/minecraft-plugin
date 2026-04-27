package ru.abstractmenus.impl;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.Catalog;
import ru.abstractmenus.api.ProviderRegistry;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.TypeRegistry;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.variables.VariableManager;
import ru.abstractmenus.hocon.api.serialize.NodeSerializers;

import java.util.Optional;

/**
 * Main implementation of {@link AbstractMenusApi}. Instantiated once during
 * plugin enable; published to Bukkit's ServicesManager so
 * {@link AbstractMenusApi#get()} resolves it.
 */
public final class AbstractMenusApiImpl implements AbstractMenusApi {

    private final AbstractMenus plugin;
    private final NodeSerializers serializers = NodeSerializers.defaults();

    private final TypeRegistry<Action> actions;
    private final TypeRegistry<Rule> rules;
    private final TypeRegistry<Activator> activators;
    private final TypeRegistry<ItemProperty> itemProperties;
    private final TypeRegistry<Catalog<?>> catalogs;
    private final ProviderRegistry providers;

    public AbstractMenusApiImpl(AbstractMenus plugin) {
        this.plugin = plugin;
        this.actions = new TypeRegistryImpl<>(serializers);
        this.rules = new TypeRegistryImpl<>(serializers);
        this.activators = new TypeRegistryImpl<>(serializers);
        this.itemProperties = new TypeRegistryImpl<>(serializers);
        this.catalogs = new TypeRegistryImpl<>(serializers);
        ProviderRegistryImpl providerImpl = new ProviderRegistryImpl();
        providerImpl.setConfigDefaults(kind -> plugin.getMainConfig().providerDefault(kind));
        this.providers = providerImpl;
    }

    @Override
    public TypeRegistry<Action> actions() {
        return actions;
    }

    @Override
    public TypeRegistry<Rule> rules() {
        return rules;
    }

    @Override
    public TypeRegistry<Activator> activators() {
        return activators;
    }

    @Override
    public TypeRegistry<ItemProperty> itemProperties() {
        return itemProperties;
    }

    @Override
    public TypeRegistry<Catalog<?>> catalogs() {
        return catalogs;
    }

    @Override
    public ProviderRegistry providers() {
        return providers;
    }

    @Override
    public NodeSerializers serializers() {
        return serializers;
    }

    @Override
    public VariableManager variables() {
        return plugin.getVariableManager();
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void loadMenus() {
        plugin.loadMenus();
    }

    @Override
    public void openMenu(Activator activator, Object ctx, Player player, Menu menu) {
        plugin.openMenu(activator, ctx, player, menu);
    }

    @Override
    public void openMenu(Player player, Menu menu) {
        plugin.openMenu(player, menu);
    }

    @Override
    public Optional<Menu> getOpenedMenu(Player player) {
        return plugin.getOpenedMenu(player);
    }

    @Override
    public String apiVersion() {
        // Paper 1.21+ exposes getPluginMeta().getVersion(); fall back to
        // getDescription().getVersion() if the new API isn't present.
        try {
            return plugin.getPluginMeta().getVersion();
        } catch (NoSuchMethodError | AbstractMethodError ignored) {
            return plugin.getDescription().getVersion();
        }
    }
}
