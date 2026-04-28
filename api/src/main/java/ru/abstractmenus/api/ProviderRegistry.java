package ru.abstractmenus.api;

import ru.abstractmenus.api.handler.EconomyHandler;
import ru.abstractmenus.api.handler.LevelHandler;
import ru.abstractmenus.api.handler.PermissionsHandler;
import ru.abstractmenus.api.handler.PlaceholderHandler;
import ru.abstractmenus.api.handler.SkinHandler;

/**
 * Registry of pluggable handler providers (economy, permissions, levels,
 * placeholders, skins). Replaces the old static {@code Handlers} facade with
 * an owner-aware registry that supports multiple providers per section plus
 * priority-based auto-resolution and a configurable default.
 *
 * <p>Each section ({@link #economy()}, {@link #permissions()},
 * {@link #levels()}, {@link #placeholders()}, {@link #skins()}) returns a
 * typed {@link ProviderSection} you register on and resolve from. The
 * registry itself is just five getters - all per-type behaviour lives on
 * {@link ProviderSection}, so adding a sixth provider type later means
 * adding one method here, not five.
 *
 * <h2>Registration</h2>
 *
 * <pre>{@code
 * public final class MyEconomyAddon implements MenuExtension {
 *     @Override public void onEnable(AbstractMenusApi api) {
 *         api.providers().economy().register(
 *             "playerpoints",
 *             new PlayerPointsEconomy(pp),
 *             100,          // priority - higher wins in auto-resolve
 *             this);        // owner - AbstractMenus uses this for cleanup
 *     }
 * }
 * }</pre>
 *
 * <h2>Lookup</h2>
 *
 * <pre>{@code
 * EconomyHandler eco       = api.providers().economy().resolve();
 * EconomyHandler vault     = api.providers().economy().resolve("vault");
 * boolean hasPP            = api.providers().economy().has("playerpoints");
 * Collection<EconomyHandler> all = api.providers().economy().all();
 * }</pre>
 *
 * @see ProviderSection
 * @see AbstractMenusApi#providers()
 */
public interface ProviderRegistry {

    ProviderSection<EconomyHandler> economy();

    ProviderSection<PermissionsHandler> permissions();

    ProviderSection<LevelHandler> levels();

    ProviderSection<PlaceholderHandler> placeholders();

    ProviderSection<SkinHandler> skins();
}
