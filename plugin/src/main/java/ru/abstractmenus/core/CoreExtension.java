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
 * {@code CoreExtension} instance — no static state.
 */
public final class CoreExtension implements MenuExtension {

    /**
     * Captured at {@link #onEnable} time from {@link AbstractMenusApi#apiVersion()}
     * so {@link #version()} no longer reports {@code null} for the core
     * extension. Resolved lazily because at {@link #onLoad} time the API
     * is already wired but version-string resolution is cheap regardless.
     */
    private String version;

    @Override
    public String name() {
        return "AbstractMenus-Core";
    }

    @Override
    public String version() {
        // Null between construction and onEnable - fall through to the
        // default-style "unknown" so /am addons list never shows "vnull".
        return version != null ? version : "unknown";
    }

    @Override
    public void onEnable(AbstractMenusApi api) {
        this.version = api.apiVersion();
        new CoreActionsBundle().register(api, this);
        new CoreRulesBundle().register(api, this);
        new CoreItemPropsBundle().register(api, this);
        new CoreActivatorsBundle().register(api, this);
        new CoreCatalogsBundle().register(api, this);
        new CoreProvidersBundle().register(api, this);
    }
}
