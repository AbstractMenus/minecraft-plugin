package ru.abstractmenus.api;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;

/**
 * Boolean predicate attached to a menu, item, or action &mdash; decides whether
 * the enclosing element is visible, clickable, or allowed to fire.
 *
 * <p>Core ships built-in rules such as {@code permission}, {@code hasMoney},
 * {@code level}, {@code world}, {@code region}, and many more. Addons extend
 * this surface by implementing {@code Rule} plus an inner
 * {@link ru.abstractmenus.hocon.api.serialize.NodeSerializer NodeSerializer}
 * and registering both through {@link AbstractMenusApi#rules()} during
 * {@link MenuExtension#onEnable}.
 *
 * <h2>Example &mdash; a cooldown rule</h2>
 *
 * <pre>{@code
 * public final class CooldownRule implements Rule {
 *
 *     private final String key;
 *     private final long seconds;
 *
 *     public CooldownRule(String key, long seconds) {
 *         this.key = key;
 *         this.seconds = seconds;
 *     }
 *
 *     @Override
 *     public boolean check(Player player, Menu menu, Item clickedItem) {
 *         return CooldownService.remaining(player.getUniqueId(), key) <= 0;
 *     }
 *
 *     public static final class Serializer implements NodeSerializer<CooldownRule> {
 *         @Override
 *         public CooldownRule deserialize(Node node, Type type) {
 *             return new CooldownRule(
 *                 node.get("key").asString(),
 *                 node.get("seconds").asLong());
 *         }
 *     }
 * }
 *
 * // Registration inside the addon's onEnable:
 * api.rules().register("cooldown",
 *         CooldownRule.class,
 *         new CooldownRule.Serializer(),
 *         this);
 * }</pre>
 *
 * <h2>Menu usage</h2>
 *
 * <pre>{@code
 * rules: [
 *   { type: cooldown, key: "daily-claim", seconds: 86400 }
 *   { type: permission, value: "server.daily" }
 * ]
 * }</pre>
 *
 * <h2>Threading &amp; purity</h2>
 *
 * {@link #check(Player, Menu, Item)} runs on the main server thread and must
 * be a <em>pure predicate</em> &mdash; do not mutate state here. It is called
 * frequently (on menu render, tick updates, every item click) so keep it
 * cheap; cache anything that hits IO.
 *
 * @see Action
 * @see Activator
 * @see TypeRegistry
 * @see AbstractMenusApi#rules()
 */
@FunctionalInterface
public interface Rule {

    /**
     * Evaluate the predicate for a given player / menu / item context.
     *
     * <p>Called whenever AbstractMenus needs to decide whether the element
     * wrapping this rule should show, fire, or be clickable. Multiple rules on
     * the same element are AND-combined.
     *
     * @param player      the player being evaluated; never {@code null} and
     *                    always online
     * @param menu        the menu the rule is evaluated against; never
     *                    {@code null}
     * @param clickedItem the item involved when the rule is evaluated as part
     *                    of a click, or {@code null} for menu-level /
     *                    item-render evaluation
     * @return {@code true} if the player satisfies the rule, {@code false}
     *         otherwise
     *
     * @implNote Must be side-effect free and cheap &mdash; runs on the main
     *           thread and is polled frequently. Do not block on IO.
     */
    boolean check(Player player, Menu menu, Item clickedItem);

}
