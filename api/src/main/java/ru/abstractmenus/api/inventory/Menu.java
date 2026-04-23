package ru.abstractmenus.api.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryHolder;
import ru.abstractmenus.api.Activator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A loaded AbstractMenus GUI &mdash; a Bukkit {@link InventoryHolder} enriched with
 * activators, rules and actions declared in a HOCON menu file.
 *
 * <p>A menu is the top-level building block of the plugin. It owns a fixed-size
 * grid of {@link Item items}, one or more {@link Activator activators} that decide
 * how the menu is triggered (commands, item-use, proximity, &hellip;), and a
 * lifecycle shared by every viewer:
 *
 * <ol>
 *   <li>an activator fires &mdash; the plugin resolves the target menu and calls
 *       {@link #open(Player)};</li>
 *   <li>the menu builds an {@link org.bukkit.inventory.Inventory} for the player,
 *       asking each {@link Item} to produce an {@link org.bukkit.inventory.ItemStack};</li>
 *   <li>while open, {@link #update(Player)} ticks on a fixed interval so
 *       time-sensitive properties ({@code placeholder}, {@code texture}&hellip;)
 *       refresh; {@link #refresh(Player)} and {@link #refreshItem(Slot, Player)}
 *       can be called explicitly;</li>
 *   <li>inventory clicks are routed to {@link #click(int, Player, ClickType)}
 *       which matches the slot against the menu's items and runs their actions;</li>
 *   <li>{@link #close(Player)} dismisses the inventory and releases per-viewer
 *       state.</li>
 * </ol>
 *
 * <p>Core ships three concrete implementations &mdash; a standard
 * {@code SimpleMenu}, an {@code AnimatedMenu} with frame-based animations, and a
 * {@code GeneratedMenu} whose layout is produced dynamically from a matrix.
 * Addons normally do <strong>not</strong> implement this interface directly
 * &mdash; they contribute new {@link ru.abstractmenus.api.Action Actions},
 * {@link ru.abstractmenus.api.Rule Rules}, {@link ItemProperty item properties}
 * or {@link Activator activators} that existing menu types consume.
 *
 * <h2>Opening a menu programmatically</h2>
 *
 * Never call {@link #open(Player)} directly from arbitrary code &mdash; go
 * through the API so activator wiring and context propagation are preserved:
 *
 * <pre>{@code
 * AbstractMenusApi api = AbstractMenusApi.get();
 * Menu shop = api.getMenuByName("shop");
 * api.openMenu(player, shop);
 * }</pre>
 *
 * <h2>Menu usage</h2>
 *
 * A minimal HOCON menu file producing this interface:
 *
 * <pre>{@code
 * title: "&6Shop"
 * size: 27
 * activators: [
 *   { type: command, command: shop }
 * ]
 * items: [
 *   {
 *     slot: 13
 *     material: DIAMOND
 *     name: "&bBuy diamond"
 *     rules:   [ { type: hasMoney, amount: 100 } ]
 *     actions {
 *       click: [
 *         { type: takeMoney, amount: 100 }
 *         { type: giveItem,  material: DIAMOND }
 *       ]
 *     }
 *   }
 * ]
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * Every method on this interface is invoked on the main server thread: lifecycle
 * calls come from {@code MenuManager}'s 20-tick update loop or from Bukkit
 * inventory events. Implementations may touch the Bukkit API freely but must
 * not block &mdash; long-running work should be dispatched through
 * {@code FoliaLib} and its result written back on the main thread.
 *
 * @see Item
 * @see ItemProperty
 * @see Slot
 * @see Activator
 * @see ru.abstractmenus.api.AbstractMenusApi#openMenu(Player, Menu)
 */
public interface Menu extends InventoryHolder, Cloneable {

    /**
     * Returns every {@link Activator} declared under this menu's
     * {@code activators} HOCON block.
     *
     * <p>The list is iteration-ordered to match the config file. An empty list
     * means the menu can only be opened programmatically via
     * {@link ru.abstractmenus.api.AbstractMenusApi#openMenu(Player, Menu)}.
     *
     * @return the declared activators; never {@code null}
     */
    List<Activator> getActivators();

    /**
     * Returns the activator that actually triggered <em>this</em> open session,
     * or empty when the menu was opened programmatically.
     *
     * <p>Populated by the core when it invokes
     * {@link ru.abstractmenus.api.AbstractMenusApi#openMenu(Activator, Object, Player, Menu)};
     * useful for actions that want to inspect the trigger (e.g. the command
     * arguments, the clicked block, &hellip;).
     *
     * @return the firing activator wrapped in an {@link Optional}, empty for
     *         programmatic opens
     */
    Optional<Activator> getActivatedBy();

    /**
     * Returns the activation context object attached at open-time.
     *
     * <p>Semantics depend on the firing {@link Activator}: a
     * {@code PlayerInteractEvent} for {@code interact}, an {@code Entity} for
     * {@code npcClick}, a {@code Block} for {@code blockClick}, and so on.
     * Used by {@code ValueExtractor}s to resolve context-scoped placeholders.
     *
     * @return the activation context wrapped in an {@link Optional}, empty for
     *         programmatic opens
     *
     * @see ru.abstractmenus.api.ValueExtractor
     */
    Optional<Object> getContext();

    /**
     * Looks up an already-rendered item at the given raw inventory slot.
     *
     * <p>This reads the materialized snapshot displayed to the current viewer
     * &mdash; not the declaration order from the HOCON file. Returns the
     * backing {@link Item} or {@code null} if the slot is empty or outside the
     * menu bounds.
     *
     * @param slot inventory slot index; {@code 0 .. getSize() - 1}
     * @return the item at that slot, or {@code null} if none
     */
    Item getItem(int slot);

    /**
     * Returns every item declared under the menu's {@code items} block.
     *
     * <p>Declaration order is preserved; items with matrix / range slots count
     * once regardless of how many slots they occupy.
     *
     * @return all declared items; never {@code null}
     */
    Collection<Item> getItems();

    /**
     * Returns the inventory size, always a multiple of {@code 9}.
     *
     * @return the current menu size in slots
     */
    int getSize();

    /**
     * Resizes the menu.
     *
     * <p>Only takes effect for subsequent {@link #open(Player)} calls &mdash;
     * already-open viewers keep the old inventory until they close and reopen.
     *
     * @param size new size; must be a multiple of {@code 9} and within Bukkit's
     *             inventory limits
     */
    void setSize(int size);

    /**
     * Opens this menu for {@code player}, evaluating top-level rules first.
     *
     * <p>Prefer {@link ru.abstractmenus.api.AbstractMenusApi#openMenu(Player, Menu)}
     * so the core can wire an activator and context for you &mdash; direct
     * calls to this method skip that bookkeeping.
     *
     * @param player the viewer; must be online
     * @return {@code true} if the inventory was shown, {@code false} when a
     *         top-level rule rejected the open
     *
     * @implNote Core impls dispatch the returned {@code boolean} to the caller
     *           of {@code AbstractMenusApi.openMenu} so callers can react to
     *           rule-denied opens.
     */
    boolean open(Player player);

    /**
     * Rebuilds every item for {@code player} and pushes the new stacks into the
     * open inventory.
     *
     * <p>Called automatically on the 20-tick update cycle for time-sensitive
     * properties and on activator-driven events; invoke manually after mutating
     * a variable that an item reads.
     *
     * @param player the viewer whose inventory to refresh
     */
    void refresh(Player player);

    /**
     * Rebuilds a single item and writes it back to the inventory.
     *
     * <p>Preferred over {@link #refresh(Player)} when only one slot changed,
     * to avoid re-running every item's properties and rules.
     *
     * @param slot   the slot(s) to refresh &mdash; a {@link Slot} may resolve
     *               to multiple inventory indices
     * @param player the viewer whose inventory to update
     */
    void refreshItem(Slot slot, Player player);

    /**
     * Temporarily overrides a slot's {@link Item} without re-running the menu
     * pipeline.
     *
     * <p>The override lives only in this viewer's session &mdash; the next
     * {@link #refresh(Player)} or reopen restores the declared item.
     *
     * @param slot   target slot(s)
     * @param item   the item to display, or {@code null} to clear
     * @param player the viewer receiving the override
     */
    void setItem(Slot slot, Item item, Player player);

    /**
     * Per-tick update hook driven by {@code MenuManager}'s 20-tick loop.
     *
     * <p>Default impls call {@link #refresh(Player)} when the menu declares an
     * {@code updateInterval}. Usually not invoked by addon code.
     *
     * @param player the viewer whose open session to tick
     */
    void update(Player player);

    /**
     * Closes this menu for {@code player} and also closes their inventory.
     *
     * <p>Shorthand for {@code close(player, true)}.
     *
     * @param player the viewer
     */
    default void close(Player player) {
        close(player, true);
    }

    /**
     * Releases per-viewer state and optionally closes the open inventory.
     *
     * <p>Pass {@code false} when the caller is about to open another menu
     * &mdash; Bukkit fires a spurious close event whenever the inventory
     * switches, and the two-argument overload lets core suppress duplicate
     * bookkeeping.
     *
     * @param player         the viewer
     * @param closeInventory whether to also call {@link Player#closeInventory()}
     */
    void close(Player player, boolean closeInventory);

    /**
     * Dispatches a click event to the item bound to {@code slot}.
     *
     * <p>Runs the item's rules first; only if every rule passes does the item's
     * matching click-action list fire. Invoked by core's inventory listener
     * &mdash; addon code rarely calls this directly.
     *
     * @param slot   raw inventory slot index from the click event
     * @param player the clicker
     * @param type   left / right / shift / number-key &hellip;
     */
    void click(int slot, Player player, ClickType type);

    /**
     * Returns a deep copy suitable for per-viewer use.
     *
     * <p>Menus are loaded once from disk and cloned per open session so
     * per-viewer state (overridden items, generated layouts) doesn't leak
     * between players.
     *
     * @return an independent copy of this menu
     */
    Menu clone();
}
