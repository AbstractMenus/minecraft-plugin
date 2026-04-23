package ru.abstractmenus.api.handler;

import org.bukkit.entity.Player;

/**
 * Handler for experience and level operations invoked by XP/level-related menu
 * actions and rules.
 *
 * <p>A {@code LevelHandler} adapts a specific levelling backend (vanilla
 * Minecraft XP bar, MMOCore, Heroes, AureliumSkills, &hellip;) to the contract
 * AbstractMenus calls when a menu declares a level-aware element:
 *
 * <ul>
 *   <li>the {@code giveXp} / {@code takeXp} actions call
 *       {@link #giveXp(Player, int)} and {@link #takeXp(Player, int)};</li>
 *   <li>the {@code giveLevel} / {@code takeLevel} actions call
 *       {@link #giveLevel(Player, int)} and {@link #takeLevel(Player, int)};</li>
 *   <li>the {@code xp} rule calls {@link #getXp(Player)} and the
 *       {@code level} rule calls {@link #getLevel(Player)} to decide whether
 *       the element is shown to the player.</li>
 * </ul>
 *
 * <h2>Registration</h2>
 *
 * Multiple handlers may coexist &mdash; the highest-priority one is picked
 * when a menu does not name a provider explicitly. Register yours via
 * {@link ru.abstractmenus.api.ProviderRegistry#registerLevels} inside your
 * addon's {@link ru.abstractmenus.api.MenuExtension#onEnable}.
 *
 * <h2>Example &mdash; bridging MMOCore</h2>
 *
 * MMOCore tracks class-specific experience and levels that live outside the
 * vanilla XP bar. A bridge delegates to its {@code PlayerData} API:
 *
 * <pre>{@code
 * public final class MMOCoreLevels implements LevelHandler {
 *
 *     @Override
 *     public int getXp(Player p) {
 *         return (int) PlayerData.get(p).getExperience();
 *     }
 *
 *     @Override
 *     public void giveXp(Player p, int xp) {
 *         PlayerData.get(p).giveExperience(xp, EXPSource.SOURCE_MENU);
 *     }
 *
 *     @Override
 *     public void takeXp(Player p, int xp) {
 *         PlayerData.get(p).giveExperience(-xp, EXPSource.SOURCE_MENU);
 *     }
 *
 *     @Override
 *     public int getLevel(Player p) {
 *         return PlayerData.get(p).getLevel();
 *     }
 *
 *     @Override
 *     public void giveLevel(Player p, int level) {
 *         PlayerData.get(p).giveLevels(level, EXPSource.SOURCE_MENU);
 *     }
 *
 *     @Override
 *     public void takeLevel(Player p, int level) {
 *         PlayerData.get(p).giveLevels(-level, EXPSource.SOURCE_MENU);
 *     }
 * }
 * }</pre>
 *
 * <h2>Menu usage</h2>
 *
 * Once a handler is registered, menus use XP/level primitives without caring
 * which backing plugin is wired up:
 *
 * <pre>{@code
 * rules: [
 *   { type: level, value: ">=10" }
 * ]
 * actions {
 *   click: [
 *     { type: takeXp,    value: 50 }
 *     { type: giveLevel, value: 1  }
 *   ]
 * }
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * All methods are called on the main server thread in response to inventory
 * events. Implementations may touch the Bukkit API directly and should avoid
 * blocking IO; if the underlying levelling plugin hits a database, serve the
 * last known value from memory and refresh asynchronously.
 *
 * @see ru.abstractmenus.api.ProviderRegistry#registerLevels
 * @see EconomyHandler
 * @see PermissionsHandler
 */
public interface LevelHandler {

    /**
     * Returns the player's current experience count. Backs the {@code xp}
     * menu rule.
     *
     * <p>This is a pure getter &mdash; implementations must not mutate state.
     *
     * @param player the player being inspected; never {@code null} and always
     *               online (called in response to an inventory event)
     * @return the player's current XP count; {@code 0} if the player is
     *         unknown to the backing levelling plugin
     *
     * @implNote Callers assume this is cheap &mdash; avoid blocking IO. If the
     *           levelling plugin requires IO, serve the last cached value and
     *           refresh asynchronously.
     */
    int getXp(Player player);

    /**
     * Adds {@code xp} experience points to the player. Backs the
     * {@code giveXp} menu action.
     *
     * @param player the player to credit; never {@code null}, always online
     * @param xp     the XP to grant; non-negative
     *
     * @implNote If the backend exposes a named XP source (MMOCore's
     *           {@code EXPSource}, Heroes' {@code ExperienceType}), tag the
     *           grant as originating from a menu so the backend can treat it
     *           differently from mob-kill XP when needed.
     */
    void giveXp(Player player, int xp);

    /**
     * Deducts {@code xp} experience points from the player. Backs the
     * {@code takeXp} menu action.
     *
     * <p>AbstractMenus does <strong>not</strong> re-check the XP before
     * calling this method, even when the menu pairs the action with an
     * {@code xp} rule. Implementations that need to reject the withdrawal
     * (e.g. the player's XP went negative between the check and the call)
     * should fail silently or clamp to zero &mdash; there is no way to cancel
     * an in-flight action from the handler.
     *
     * @param player the player to charge; never {@code null}, always online
     * @param xp     the XP to withdraw; non-negative
     */
    void takeXp(Player player, int xp);

    /**
     * Returns the player's current level. Backs the {@code level} menu rule.
     *
     * <p>This is a pure getter &mdash; implementations must not mutate state.
     *
     * @param player the player being inspected; never {@code null} and always
     *               online
     * @return the player's current level; {@code 0} if the player is unknown
     *         to the backing levelling plugin
     *
     * @implNote Callers assume this is cheap &mdash; avoid blocking IO. Cache
     *           and refresh asynchronously if the backend requires IO.
     */
    int getLevel(Player player);

    /**
     * Adds {@code level} levels to the player. Backs the {@code giveLevel}
     * menu action.
     *
     * @param player the player to credit; never {@code null}, always online
     * @param level  the number of levels to grant; non-negative
     */
    void giveLevel(Player player, int level);

    /**
     * Deducts {@code level} levels from the player. Backs the
     * {@code takeLevel} menu action.
     *
     * <p>AbstractMenus does <strong>not</strong> re-check the level before
     * calling this method. Implementations that need to reject the withdrawal
     * should fail silently or clamp to zero.
     *
     * @param player the player to charge; never {@code null}, always online
     * @param level  the number of levels to withdraw; non-negative
     */
    void takeLevel(Player player, int level);

}
