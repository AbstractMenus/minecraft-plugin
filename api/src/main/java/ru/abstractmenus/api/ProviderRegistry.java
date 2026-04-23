package ru.abstractmenus.api;

import ru.abstractmenus.api.handler.EconomyHandler;
import ru.abstractmenus.api.handler.LevelHandler;
import ru.abstractmenus.api.handler.PermissionsHandler;
import ru.abstractmenus.api.handler.PlaceholderHandler;
import ru.abstractmenus.api.handler.SkinHandler;

import java.util.Collection;

/**
 * Registry of pluggable handler providers (economy, permissions, levels,
 * placeholders, skins). Replaces the old static {@code Handlers.set*()/get*()}
 * facade with an owner-aware registry that supports multiple providers per
 * section plus priority-based auto-resolution.
 *
 * <h2>Registration</h2>
 *
 * <pre>{@code
 * public final class MyEconomyAddon implements MenuExtension {
 *     @Override public void onEnable(AbstractMenusApi api) {
 *         api.providers().registerEconomy(
 *             "playerpoints",
 *             new PlayerPointsEconomy(pp),
 *             100,          // priority — higher wins in auto-resolve
 *             this);        // owner for unregisterAll on reload
 *     }
 * }
 * }</pre>
 *
 * <h2>Resolution</h2>
 *
 * <ul>
 *   <li>{@link #economy()} &mdash; highest-priority registered handler, or
 *       first-registered on ties. Returns {@code null} if none registered.</li>
 *   <li>{@link #economy(String)} &mdash; explicit lookup by id.</li>
 *   <li>{@link #allEconomy()} &mdash; every registration, for introspection.</li>
 *   <li>{@link #hasEconomy(String)} &mdash; validation helper (used by
 *       menu-serializers to fail-at-load when a HOCON file references an
 *       unknown provider).</li>
 * </ul>
 *
 * <p>Same shape for permissions / levels / placeholders / skins.
 *
 * @see AbstractMenusApi#providers()
 */
public interface ProviderRegistry {

    // ---- Economy ---------------------------------------------------------

    void registerEconomy(String id, EconomyHandler handler, int priority, MenuExtension owner);
    EconomyHandler economy();
    EconomyHandler economy(String id);
    Collection<EconomyHandler> allEconomy();
    boolean hasEconomy(String id);

    // ---- Permissions -----------------------------------------------------

    void registerPermissions(String id, PermissionsHandler handler, int priority, MenuExtension owner);
    PermissionsHandler permissions();
    PermissionsHandler permissions(String id);
    Collection<PermissionsHandler> allPermissions();
    boolean hasPermissions(String id);

    // ---- Levels ----------------------------------------------------------

    void registerLevels(String id, LevelHandler handler, int priority, MenuExtension owner);
    LevelHandler levels();
    LevelHandler levels(String id);
    Collection<LevelHandler> allLevels();
    boolean hasLevels(String id);

    // ---- Placeholders ----------------------------------------------------

    void registerPlaceholders(String id, PlaceholderHandler handler, int priority, MenuExtension owner);
    PlaceholderHandler placeholders();
    PlaceholderHandler placeholders(String id);
    Collection<PlaceholderHandler> allPlaceholders();
    boolean hasPlaceholders(String id);

    // ---- Skins -----------------------------------------------------------

    void registerSkins(String id, SkinHandler handler, int priority, MenuExtension owner);
    SkinHandler skins();
    SkinHandler skins(String id);
    Collection<SkinHandler> allSkins();
    boolean hasSkins(String id);

    // ---- Cleanup ---------------------------------------------------------

    /**
     * Remove every provider registration (across all sections) owned by
     * {@code owner}. Called by AddonManager when an addon is disabled.
     *
     * @param owner the extension whose providers should be cleared
     */
    void unregisterAll(MenuExtension owner);
}
