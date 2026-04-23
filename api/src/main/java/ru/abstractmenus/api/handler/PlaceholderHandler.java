package ru.abstractmenus.api.handler;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Handler for placeholder substitution across every user-facing string a menu
 * produces &mdash; item names, lore, titles, action arguments, rule values.
 *
 * <p>A {@code PlaceholderHandler} adapts a specific placeholder engine
 * (PlaceholderAPI, MVdWPlaceholderAPI, &hellip;) to the contract AbstractMenus
 * calls everywhere a string might contain {@code %name%}-style tokens:
 *
 * <ul>
 *   <li>{@link #replace(Player, String)} is invoked for every single-line
 *       string rendered into an inventory (display names, menu titles,
 *       action arguments);</li>
 *   <li>{@link #replace(Player, List)} is invoked for every multi-line string
 *       (item lore);</li>
 *   <li>{@link #replacePlaceholder(Player, String)} is invoked for individual
 *       placeholder resolution from code paths that already isolate the
 *       token &mdash; it is <strong>not</strong> the main entry point for
 *       menu rendering.</li>
 * </ul>
 *
 * <p>Unlike economy or level handlers, placeholder substitution is pervasive:
 * it runs on essentially every string the player sees. Implementations must
 * be fast and tolerant of malformed input &mdash; never throw from the
 * {@code replace} methods, return the original string instead.
 *
 * <h2>Registration</h2>
 *
 * Although the placeholder handler lives in
 * {@link ru.abstractmenus.api.ProviderRegistry} for API symmetry with the
 * other sections, in practice it is registered <strong>once globally</strong>
 * via {@link ru.abstractmenus.api.ProviderRegistry#registerPlaceholders} at
 * {@link ru.abstractmenus.api.MenuExtension#onEnable} time, not selected
 * per-action. Per-element provider selection is not meaningful for
 * placeholders &mdash; the core engine (PAPI) is chain-of-responsibility by
 * design.
 *
 * <h2>Example &mdash; bridging PlaceholderAPI</h2>
 *
 * PAPI exposes a single facade that covers every registered expansion:
 *
 * <pre>{@code
 * public final class PapiPlaceholders implements PlaceholderHandler {
 *
 *     @Override
 *     public String replacePlaceholder(Player p, String placeholder) {
 *         return PlaceholderAPI.setPlaceholders(p, "%" + placeholder + "%");
 *     }
 *
 *     @Override
 *     public String replace(Player p, String str) {
 *         if (str == null || str.isEmpty()) return str;
 *         return PlaceholderAPI.setPlaceholders(p, str);
 *     }
 *
 *     @Override
 *     public List<String> replace(Player p, List<String> list) {
 *         if (list == null || list.isEmpty()) return list;
 *         return PlaceholderAPI.setPlaceholders(p, list);
 *     }
 *
 *     @Override
 *     public void registerAll() {
 *         new AbstractMenusExpansion().register();
 *     }
 * }
 * }</pre>
 *
 * <h2>Menu usage</h2>
 *
 * Once a handler is registered, any string in any menu file may contain
 * placeholders and they will be resolved per-viewer at render time:
 *
 * <pre>{@code
 * items {
 *   stats {
 *     material: BOOK
 *     name: "&6Welcome, %player_name%"
 *     lore: [
 *       "&7Balance: &e%vault_eco_balance%"
 *       "&7Level:   &a%player_level%"
 *     ]
 *   }
 * }
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * All methods are called on the main server thread during inventory render.
 * Because they run per-item per-tick while a menu is open, blocking IO here
 * will freeze the server &mdash; PAPI expansions that hit a database must
 * cache aggressively and serve stale values. The engine itself should never
 * block.
 *
 * @see ru.abstractmenus.api.ProviderRegistry#registerPlaceholders
 * @see EconomyHandler
 */
public interface PlaceholderHandler {

    /**
     * Resolves a single placeholder token (without surrounding {@code %}
     * characters) for the given player.
     *
     * <p>Used by code paths that already isolated the placeholder key.
     * Prefer {@link #replace(Player, String)} for free-form strings &mdash;
     * this method is not the main entry point for menu rendering.
     *
     * @param player      the context player; never {@code null}, always
     *                    online
     * @param placeholder the placeholder key <strong>without</strong> the
     *                    surrounding {@code %} characters
     *                    (e.g. {@code "player_name"}); never {@code null}
     * @return the resolved value, or the original token surrounded by
     *         {@code %} if the placeholder is unknown to the backend
     *
     * @implNote Implementations that wrap a token-by-token engine may simply
     *           delegate to {@link #replace(Player, String)} after wrapping
     *           the key in {@code %}.
     */
    String replacePlaceholder(Player player, String placeholder);

    /**
     * Resolves every placeholder in {@code str} for the given player.
     *
     * <p>This is the hot path for menu rendering: AbstractMenus calls it for
     * display names, titles, and action arguments on every redraw. Never
     * throw &mdash; return {@code str} unchanged if the backend errors.
     *
     * @param player the context player; never {@code null}, always online
     * @param str    the string to process; may be {@code null} or empty, in
     *               which case it is returned as-is
     * @return the string with every recognised placeholder substituted;
     *         unknown placeholders are returned verbatim so the user sees
     *         {@code %missing%} rather than an empty slot
     *
     * @implNote Short-circuit on {@code null}/empty input before calling the
     *           backend &mdash; PAPI tolerates both but the call is not free.
     */
    String replace(Player player, String str);

    /**
     * Resolves every placeholder in each element of {@code list} for the
     * given player. Used for item lore and other multi-line strings.
     *
     * <p>Implementations typically delegate to
     * {@link #replace(Player, String)} per element, but may batch when the
     * backend supports it.
     *
     * @param player the context player; never {@code null}, always online
     * @param list   the list to process; may be {@code null} or empty, in
     *               which case it is returned as-is
     * @return a list with placeholders substituted. Implementations may
     *         return the same list instance (mutated) or a new list &mdash;
     *         callers must not assume either
     *
     * @implNote Never throw; if one element fails, log and keep processing
     *           the rest.
     */
    List<String> replace(Player player, List<String> list);

    /**
     * Registers AbstractMenus' own placeholders (menu-scoped variables,
     * built-ins) with the backing engine.
     *
     * <p>Invoked once during
     * {@link ru.abstractmenus.api.MenuExtension#onEnable} after the handler
     * is wired up, so that other plugins querying PAPI can resolve
     * AbstractMenus' placeholders too. Safe to call multiple times &mdash;
     * implementations should guard against double-registration if the
     * backend rejects duplicates.
     *
     * @implNote For PAPI, instantiate and {@code register()} your
     *           {@code PlaceholderExpansion}. For backends without a
     *           registration concept, treat this as a no-op.
     */
    void registerAll();

}
