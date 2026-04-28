package ru.abstractmenus.api.handler;

import org.bukkit.entity.Player;

/**
 * Handler for economy operations invoked by money-related menu actions and rules.
 *
 * <p>An {@code EconomyHandler} adapts a specific economy plugin
 * (Vault + EssentialsX, PlayerPoints, TokenManager, &hellip;) to the contract
 * AbstractMenus calls when a menu declares a money-aware element:
 *
 * <ul>
 *   <li>the {@code takeMoney} / {@code giveMoney} actions call
 *       {@link #takeBalance(Player, double)} and
 *       {@link #giveBalance(Player, double)} respectively;</li>
 *   <li>the {@code hasMoney} rule calls {@link #hasBalance(Player, double)}
 *       to decide whether the element is shown to the player.</li>
 * </ul>
 *
 * <h2>Registration</h2>
 *
 * A single handler is active at a time. Register yours via
 * {@link ru.abstractmenus.api.ProviderSection#register} inside your
 * addon's {@link ru.abstractmenus.api.MenuExtension#onEnable}:
 *
 * <h2>Example &mdash; bridging PlayerPoints</h2>
 *
 * Unlike Vault's {@code double}-based currency, PlayerPoints stores an
 * {@code int} token count. The handler rounds fractional values towards zero:
 *
 * <pre>{@code
 * public final class PlayerPointsEconomy implements EconomyHandler {
 *
 *     private final PlayerPointsAPI pp;
 *
 *     public PlayerPointsEconomy(PlayerPointsAPI pp) { this.pp = pp; }
 *
 *     @Override
 *     public boolean hasBalance(Player p, double balance) {
 *         return pp.look(p.getUniqueId()) >= (int) balance;
 *     }
 *
 *     @Override
 *     public void takeBalance(Player p, double amount) {
 *         pp.take(p.getUniqueId(), (int) amount);
 *     }
 *
 *     @Override
 *     public void giveBalance(Player p, double amount) {
 *         pp.give(p.getUniqueId(), (int) amount);
 *     }
 * }
 * }</pre>
 *
 * <h2>Menu usage</h2>
 *
 * Once a handler is registered, menus use money primitives without caring
 * which backing plugin is wired up:
 *
 * <pre>{@code
 * rules: [
 *   { type: hasMoney, amount: 100 }
 * ]
 * actions {
 *   click: [
 *     { type: takeMoney, amount: 100 }
 *     { type: giveItem,  ... }
 *   ]
 * }
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * All methods are called on the main server thread in response to inventory
 * events. Implementations may touch the Bukkit API directly and should avoid
 * blocking IO; if the underlying economy plugin hits a database, cache the
 * last known balance and refresh asynchronously.
 *
 * @see ru.abstractmenus.api.ProviderSection#register
 * @see PermissionsHandler
 * @see LevelHandler
 */
public interface EconomyHandler {

    /**
     * Returns {@code true} if {@code player} has at least {@code balance}
     * in this economy. Backs the {@code hasMoney} menu rule.
     *
     * <p>This is a pure predicate &mdash; implementations must not mutate
     * state. Use {@link #takeBalance(Player, double)} to withdraw after a
     * successful check.
     *
     * @param player  the player being checked; never {@code null} and always
     *                online (called in response to an inventory event)
     * @param balance the required amount; non-negative
     * @return {@code true} if the player has at least {@code balance},
     *         {@code false} otherwise &mdash; including when the player is
     *         unknown to the backing economy plugin
     *
     * @implNote Callers assume this is cheap &mdash; avoid blocking IO. If
     *           the economy plugin requires IO, serve the last cached value
     *           and refresh asynchronously.
     */
    boolean hasBalance(Player player, double balance);

    /**
     * Deducts {@code amount} from {@code player}'s balance. Backs the
     * {@code takeMoney} menu action.
     *
     * <p>AbstractMenus does <strong>not</strong> re-check the balance before
     * calling this method, even when the menu pairs the action with a
     * {@code hasMoney} rule. Implementations that need to reject the
     * withdrawal (e.g. the balance went negative between the check and the
     * call) should fail silently &mdash; there is no way to cancel an
     * in-flight action from the handler.
     *
     * @param player the player to charge; never {@code null}, always online
     * @param amount the amount to withdraw; non-negative. Handlers bridging
     *               an integer currency (PlayerPoints, TokenManager) should
     *               round towards zero ({@code (int) amount}) to match
     *               {@link #giveBalance(Player, double)}.
     */
    void takeBalance(Player player, double amount);

    /**
     * Adds {@code amount} to {@code player}'s balance. Backs the
     * {@code giveMoney} menu action.
     *
     * @param player the player to credit; never {@code null}, always online
     * @param amount the amount to deposit; non-negative
     *
     * @implNote Handlers bridging an integer currency should round
     *           {@code amount} towards zero, matching
     *           {@link #takeBalance(Player, double)}.
     */
    void giveBalance(Player player, double amount);
}
