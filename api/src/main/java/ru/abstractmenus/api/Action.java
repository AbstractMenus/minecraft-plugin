package ru.abstractmenus.api;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;

/**
 * Executable menu action &mdash; the imperative "do something" block attached to
 * a menu item, a menu itself, or an {@link Activator}.
 *
 * <p>Core ships dozens of built-in actions ({@code openMenu}, {@code takeMoney},
 * {@code giveItem}, {@code runCommand}, {@code sendMessage}, &hellip;). Addons
 * extend this surface by implementing {@code Action} plus an inner
 * {@link ru.abstractmenus.hocon.api.serialize.NodeSerializer NodeSerializer}
 * and registering both through {@link AbstractMenusApi#actions()} during
 * {@link MenuExtension#onEnable}.
 *
 * <h2>Example &mdash; a webhook action</h2>
 *
 * <pre>{@code
 * public final class SendWebhookAction implements Action {
 *
 *     private final String url;
 *
 *     public SendWebhookAction(String url) { this.url = url; }
 *
 *     @Override
 *     public void activate(Player player, Menu menu, Item clickedItem) {
 *         HttpClients.postAsync(url, "{\"user\":\"" + player.getName() + "\"}");
 *     }
 *
 *     public static final class Serializer implements NodeSerializer<SendWebhookAction> {
 *         @Override
 *         public SendWebhookAction deserialize(Node node, Type type) {
 *             return new SendWebhookAction(node.get("url").asString());
 *         }
 *     }
 * }
 *
 * // Registration inside the addon's onEnable:
 * api.actions().register("sendWebhook",
 *         SendWebhookAction.class,
 *         new SendWebhookAction.Serializer(),
 *         this);
 * }</pre>
 *
 * <h2>Menu usage</h2>
 *
 * Once registered, the action is available to every menu by its HOCON key:
 *
 * <pre>{@code
 * actions {
 *   click: [
 *     { type: sendWebhook, url: "https://example.com/hook" }
 *     { type: closeMenu }
 *   ]
 * }
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * {@link #activate(Player, Menu, Item)} is invoked on the main server thread
 * in response to an inventory event (click, close) or an activator firing.
 * Implementations may touch the Bukkit API directly; offload blocking IO to a
 * scheduler.
 *
 * @see Rule
 * @see Activator
 * @see TypeRegistry
 * @see AbstractMenusApi#actions()
 */
@FunctionalInterface
public interface Action {

    /**
     * Execute this action in the context of a menu interaction.
     *
     * <p>Called by AbstractMenus when the action's enclosing list fires &mdash;
     * typically a click on a menu item, an activator trigger, or a menu open /
     * close hook.
     *
     * @param player      the player who triggered the action; never
     *                    {@code null} and always online
     * @param menu        the menu in which the action fires; never {@code null}
     * @param clickedItem the item that triggered the action, or {@code null}
     *                    when the action was fired by something other than an
     *                    item click (activator, menu-level hook)
     *
     * @implNote Runs on the main server thread. Do not block on IO &mdash;
     *           schedule long-running work asynchronously.
     */
    void activate(Player player, Menu menu, Item clickedItem);

}
