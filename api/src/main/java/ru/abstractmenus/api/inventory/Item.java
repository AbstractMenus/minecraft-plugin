package ru.abstractmenus.api.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * A single declared slot-filler inside a {@link Menu} &mdash; the bridge between
 * a HOCON item block and the Bukkit {@link ItemStack} shown to a viewer.
 *
 * <p>An item holds a map of named {@link ItemProperty ItemProperties}
 * (material, display-name, lore, texture, glow, &hellip;) plus per-item rules
 * and actions stored separately by the containing menu. When the menu is
 * rendered, {@link #build(Player, Menu)} walks every property in registration
 * order, letting each mutate the {@link ItemStack} / {@link org.bukkit.inventory.meta.ItemMeta}.
 *
 * <p>Addon authors <strong>do not</strong> implement this interface. Instead,
 * contribute new {@link ItemProperty} types via
 * {@link ru.abstractmenus.api.AbstractMenusApi#itemProperties()} &mdash; core
 * will instantiate and attach them automatically when HOCON parsing encounters
 * the matching key.
 *
 * <h2>Menu usage</h2>
 *
 * Every top-level key inside an item block is parsed as an {@link ItemProperty}:
 *
 * <pre>{@code
 * items: [
 *   {
 *     slot: 4
 *     material: PLAYER_HEAD    # PropMaterial
 *     name: "&6%player_name%"  # PropName
 *     lore: [ "&7Online", "" ] # PropLore
 *     texture: "%player_name%" # PropTexture
 *   }
 * ]
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * Item construction runs on the main server thread inside menu render and
 * refresh. Implementations should not block.
 *
 * @see ItemProperty
 * @see Menu
 */
public interface Item extends Cloneable {

    /**
     * Returns all attached properties keyed by their HOCON name
     * ({@code "material"}, {@code "name"}, {@code "lore"}, &hellip;).
     *
     * <p>The returned map is the live backing store &mdash; mutating it
     * affects subsequent {@link #build(Player, Menu)} calls.
     *
     * @return the live property map; never {@code null}
     */
    Map<String, ItemProperty> getProperties();

    /**
     * Attaches a single property, replacing any existing entry under
     * {@code key}.
     *
     * @param key      HOCON-visible property name (e.g. {@code "lore"})
     * @param property the property instance to attach
     */
    void addProperty(String key, ItemProperty property);

    /**
     * Replaces the entire property map.
     *
     * <p>Used by serializers when re-loading an item from config. Normal code
     * should prefer {@link #addProperty(String, ItemProperty)} /
     * {@link #removeProperty(String)} to avoid wiping rules-relevant state.
     *
     * @param properties the new property map; never {@code null}
     */
    void setProperties(Map<String, ItemProperty> properties);

    /**
     * Detaches a property by key.
     *
     * @param key HOCON-visible property name
     * @return the removed property, or {@code null} if none was attached under
     *         that key
     */
    ItemProperty removeProperty(String key);

    /**
     * Checks whether a raw {@link ItemStack} matches this item's declaration.
     *
     * <p>Used by item-requiring actions ({@code takeItem}) to find the slot
     * holding the expected item in the player's inventory. Placeholders inside
     * properties are resolved against {@code player} before comparison so
     * player-specific names and textures match correctly.
     *
     * @param item   the stack to compare against; {@code null} always yields
     *               {@code false}
     * @param player the context player for placeholder resolution
     * @return {@code true} if every material / meta aspect declared by this
     *         item matches {@code item}
     */
    boolean isSimilar(ItemStack item, Player player);

    /**
     * Materialises this item into a Bukkit {@link ItemStack} tailored for
     * {@code player}.
     *
     * <p>Properties run in registration order: those flagged
     * {@link ItemProperty#canReplaceMaterial()} run first so the stack has a
     * valid {@link org.bukkit.inventory.meta.ItemMeta} by the time the rest
     * mutate it. Placeholders inside property values are resolved against
     * {@code player}.
     *
     * @param player the viewer &mdash; used for placeholder replacement
     * @param menu   the owning menu, used by properties that need to look up
     *               the activator or activation context; may be {@code null}
     *               when building outside an open session
     * @return the rendered stack; never {@code null}
     */
    ItemStack build(Player player, Menu menu);

    /**
     * Returns a deep copy so per-viewer property tweaks don't leak to the
     * shared menu template.
     *
     * @return an independent copy of this item
     */
    Item clone();
}
