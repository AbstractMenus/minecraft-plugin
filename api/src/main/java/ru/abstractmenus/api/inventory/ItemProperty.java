package ru.abstractmenus.api.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A single mutator that contributes one aspect of a rendered {@link Item}
 * &mdash; e.g. its material, display name, lore line, player-head texture or
 * glow effect.
 *
 * <p>An item's HOCON block is parsed as a set of named properties: every
 * top-level key ({@code material}, {@code name}, {@code lore}, &hellip;) is
 * looked up in {@link ru.abstractmenus.api.AbstractMenusApi#itemProperties()},
 * deserialized and attached to the owning {@link Item}. At render time
 * {@link Item#build(Player, Menu)} walks the resulting property map and
 * invokes {@link #apply(ItemStack, ItemMeta, Player, Menu)} on each one so it
 * can mutate the outgoing {@link ItemStack} / {@link ItemMeta}.
 *
 * <p>This is the primary extension point for addons. To contribute a new
 * property, implement this interface, pair it with an inner
 * {@link ru.abstractmenus.hocon.api.serialize.NodeSerializer NodeSerializer}
 * and register both in your {@link ru.abstractmenus.api.MenuExtension#onEnable}.
 *
 * <h2>Example &mdash; a {@code glow} property</h2>
 *
 * <pre>{@code
 * public final class PropGlow implements ItemProperty {
 *
 *     private final boolean glow;
 *
 *     public PropGlow(boolean glow) { this.glow = glow; }
 *
 *     @Override public boolean canReplaceMaterial() { return false; }
 *     @Override public boolean isApplyMeta()        { return true;  }
 *
 *     @Override
 *     public void apply(ItemStack item, ItemMeta meta, Player player, Menu menu) {
 *         if (!glow) return;
 *         meta.addEnchant(Enchantment.UNBREAKING, 1, true);
 *         meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
 *     }
 *
 *     public static final class Serializer extends NodeSerializer<PropGlow> {
 *         // ... HOCON -> PropGlow
 *     }
 * }
 *
 * // registration:
 * api.itemProperties().register("glow", PropGlow.class, new PropGlow.Serializer(), this);
 * }</pre>
 *
 * <h2>Menu usage</h2>
 *
 * Once registered, the property is available by key inside any item block:
 *
 * <pre>{@code
 * items: [
 *   {
 *     slot: 13
 *     material: DIAMOND_SWORD
 *     name: "&bLegendary Blade"
 *     glow: true
 *   }
 * ]
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * {@link #apply(ItemStack, ItemMeta, Player, Menu)} runs on the main server
 * thread during item rendering &mdash; implementations may touch the Bukkit
 * API freely but must not block. Long-running work (HTTP texture fetches,
 * database reads) should be dispatched through {@code FoliaLib} and cached;
 * {@code apply} itself should only read the cache.
 *
 * @see Item
 * @see Menu
 * @see ru.abstractmenus.api.AbstractMenusApi#itemProperties()
 */
public interface ItemProperty {

    /**
     * Whether this property overwrites the item's {@link org.bukkit.Material}.
     *
     * <p>Material-replacing properties (e.g. {@code material}, {@code head},
     * {@code headTexture}) run <strong>before</strong> all others so the stack
     * has a valid {@link ItemMeta} by the time the rest mutate it &mdash;
     * Bukkit otherwise throws away meta when the material changes.
     *
     * @return {@code true} if {@link #apply(ItemStack, ItemMeta, Player, Menu)}
     *         may swap the stack's material, {@code false} for meta-only
     *         mutators
     *
     * @apiNote Return {@code false} unless you really do reassign the material.
     *          The flag only controls ordering; returning {@code true}
     *          spuriously forces the rest of the pipeline to run against a
     *          stack whose meta may have been reset.
     */
    boolean canReplaceMaterial();

    /**
     * Whether the {@link ItemMeta} argument passed to
     * {@link #apply(ItemStack, ItemMeta, Player, Menu)} should be written back
     * onto the stack automatically after {@code apply} returns.
     *
     * <p>Return {@code true} when your property only mutates meta fields
     * (display name, lore, flags, enchants) and lets the caller commit the
     * meta for you. Return {@code false} when you need to call
     * {@link ItemStack#setItemMeta(ItemMeta)} yourself &mdash; typically
     * because you swap to a different meta type or tweak the stack in ways
     * the shared commit would overwrite.
     *
     * @return {@code true} if the framework should call
     *         {@code item.setItemMeta(meta)} on your behalf after
     *         {@code apply} returns
     *
     * @apiNote Returning {@code true} and <em>also</em> calling
     *          {@code item.setItemMeta(...)} manually inside {@code apply}
     *          leads to lost writes &mdash; the framework's follow-up
     *          {@code setItemMeta} overwrites yours. Pick one.
     */
    boolean isApplyMeta();

    /**
     * Applies this property to the in-flight item stack.
     *
     * <p>Called once per render per viewer, in registration order relative to
     * the item's other properties. Placeholders inside property values should
     * be resolved against {@code player} before use so per-viewer content
     * (names, textures, balances) renders correctly.
     *
     * @param item   the stack being rendered &mdash; mutate freely, never
     *               {@code null}. Prefer mutating {@code meta} unless you
     *               specifically need to touch the stack itself.
     * @param meta   the stack's current {@link ItemMeta}; write display-name,
     *               lore, flags, enchants here. Committed automatically iff
     *               {@link #isApplyMeta()} returns {@code true}.
     * @param player the viewer for whom the item is being built; never
     *               {@code null} and always online. Use for placeholder
     *               resolution and permission-aware rendering.
     * @param menu   the owning menu; may be {@code null} when an item is built
     *               outside an open session (e.g. for {@code isSimilar}
     *               comparisons). Use to read the activator / activation
     *               context via {@link Menu#getActivatedBy()} and
     *               {@link Menu#getContext()}.
     */
    void apply(ItemStack item, ItemMeta meta, Player player, Menu menu);

}
