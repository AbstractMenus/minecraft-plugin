package ru.abstractmenus.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import ru.abstractmenus.api.inventory.Menu;

/**
 * Event-driven trigger that opens a menu in response to a game event &mdash;
 * a command, a chat line, an NPC click, a region boundary crossing, a sign
 * interaction, and so on.
 *
 * <p>Every activator is a Bukkit {@link Listener}: AbstractMenus registers the
 * activator with the server's plugin manager automatically when a menu is
 * loaded and unregisters it on menu reload / disable. <strong>Do not</strong>
 * register the listener yourself.
 *
 * <p>Core ships built-in activators such as {@code command}, {@code chat},
 * {@code clickNPC}, {@code clickEntity}, {@code regionJoin}, and
 * {@code signInteract}. Addons extend this surface by subclassing
 * {@code Activator}, annotating one or more methods with {@code @EventHandler},
 * and calling {@link #openMenu(Object, Player)} when the trigger fires.
 *
 * <h2>Example &mdash; open a menu on resource-pack load</h2>
 *
 * <pre>{@code
 * public final class PackLoadedActivator extends Activator {
 *
 *     @EventHandler
 *     public void on(PlayerResourcePackStatusEvent e) {
 *         if (e.getStatus() == Status.SUCCESSFULLY_LOADED) {
 *             openMenu(e, e.getPlayer());
 *         }
 *     }
 *
 *     @Override
 *     public ValueExtractor getValueExtractor() {
 *         return (obj, key) -> {
 *             if (!(obj instanceof PlayerResourcePackStatusEvent ev)) return null;
 *             return switch (key) {
 *                 case "status" -> ev.getStatus().name();
 *                 default -> null;
 *             };
 *         };
 *     }
 *
 *     public static final class Serializer implements NodeSerializer<PackLoadedActivator> {
 *         @Override
 *         public PackLoadedActivator deserialize(Node node, Type type) {
 *             return new PackLoadedActivator();
 *         }
 *     }
 * }
 *
 * // Registration inside the addon's onEnable:
 * api.activators().register("packLoaded",
 *         PackLoadedActivator.class,
 *         new PackLoadedActivator.Serializer(),
 *         this);
 * }</pre>
 *
 * <h2>Menu usage</h2>
 *
 * <pre>{@code
 * activators: [
 *   { type: packLoaded }
 * ]
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * Event callbacks run on whichever thread Bukkit dispatches the event from &mdash;
 * typically the main server thread. The {@link #openMenu(Object, Player)} call
 * is safe to invoke from there directly.
 *
 * @see Action
 * @see Rule
 * @see ValueExtractor
 * @see TypeRegistry
 * @see AbstractMenusApi#activators()
 */
public abstract class Activator implements Listener {

    /**
     * The menu this activator opens. Injected by AbstractMenus via
     * {@link #setTargetMenu(Menu)} right after construction and before the
     * listener is registered.
     */
    protected Menu menu;

    /**
     * Bind this activator to the menu it should open.
     *
     * <p>Called by AbstractMenus during menu load &mdash; addon code should not
     * invoke this directly.
     *
     * @param menu the target menu; never {@code null}
     */
    public void setTargetMenu(Menu menu) {
        this.menu = menu;
    }

    /**
     * Open {@link #menu} for {@code player}, carrying {@code ctx} as the
     * context object that will be passed to rules, placeholders, and the
     * {@link ValueExtractor} returned by {@link #getValueExtractor()}.
     *
     * <p>Called from event handlers in subclasses once the trigger condition
     * is satisfied.
     *
     * @param ctx    the opening context &mdash; typically the Bukkit
     *               {@code Event} that fired the trigger, but any object the
     *               activator's {@link ValueExtractor} understands is valid
     * @param player the player to open the menu for; never {@code null} and
     *               expected to be online
     *
     * @implNote Delegates to
     *           {@link AbstractMenusApi#openMenu(Activator, Object, Player, Menu)}.
     */
    protected void openMenu(Object ctx, Player player) {
        AbstractMenusApi.get().openMenu(this, ctx, player, menu);
    }

    /**
     * Value extractor used to resolve placeholders against the opening
     * context object passed to {@link #openMenu(Object, Player)}.
     *
     * <p>Override to expose event-specific fields as menu placeholders (for
     * instance, the clicked entity's UUID or a region name).
     *
     * @return the extractor, or {@code null} if this activator does not
     *         contribute any context placeholders (the default)
     */
    public ValueExtractor getValueExtractor() {
        return null;
    }

}
