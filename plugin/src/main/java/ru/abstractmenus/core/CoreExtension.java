package ru.abstractmenus.core;

import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.MenuExtension;

/**
 * The built-in "extension" that registers every core action, rule, item
 * property, activator and catalog. Lives inside the plugin itself (not
 * loaded via the addon classloader) and acts as the reference example of
 * how external addons should structure their registration.
 *
 * <p>The five bundles keep registration grouped by surface area for
 * readability. Each is a pure function of {@code api} and the owning
 * {@code CoreExtension} instance &mdash; no static state.
 */
public final class CoreExtension implements MenuExtension {

    @Override
    public String name() {
        return "AbstractMenus-Core";
    }

    @Override
    public String version() {
        // Populated from plugin version at runtime via api.apiVersion().
        return null;
    }

    @Override
    public void onEnable(AbstractMenusApi api) {
        new CoreActionsBundle().register(api, this);
        new CoreRulesBundle().register(api, this);
        new CoreItemPropsBundle().register(api, this);
        new CoreActivatorsBundle().register(api, this);
        new CoreCatalogsBundle().register(api, this);
        new CoreProvidersBundle().register(api, this);
    }
}
