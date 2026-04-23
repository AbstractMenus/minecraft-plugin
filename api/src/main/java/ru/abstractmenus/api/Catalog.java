package ru.abstractmenus.api;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;

import java.util.Collection;

/**
 * Source of data collections rendered across inventory slots by
 * {@code generatedMenu} menus.
 *
 * <p>A catalog produces a {@link Collection} of arbitrary objects
 * ({@code Player}, {@code World}, custom domain entities, &hellip;) &mdash;
 * AbstractMenus paginates the collection across the menu's
 * {@code matrix} and renders each object into the layout's item template,
 * substituting placeholders through the catalog's {@link ValueExtractor}.
 *
 * <p>Core ships built-in catalogs such as {@code players} and {@code worlds}.
 * Addons extend this surface by implementing {@code Catalog<T>} plus an inner
 * {@link ru.abstractmenus.hocon.api.serialize.NodeSerializer NodeSerializer}
 * and registering both through {@link AbstractMenusApi#catalogs()} during
 * {@link MenuExtension#onEnable}.
 *
 * <h2>Example &mdash; a clan roster catalog</h2>
 *
 * <pre>{@code
 * public final class ClanMembersCatalog implements Catalog<ClanMember> {
 *
 *     @Override
 *     public Collection<ClanMember> snapshot(Player player, Menu menu) {
 *         return ClanService.get().membersOf(player.getUniqueId());
 *     }
 *
 *     @Override
 *     public ValueExtractor extractor() {
 *         return (obj, key) -> {
 *             if (!(obj instanceof ClanMember m)) return null;
 *             return switch (key) {
 *                 case "name" -> m.name();
 *                 case "rank" -> m.rank().name();
 *                 default    -> null;
 *             };
 *         };
 *     }
 *
 *     public static final class Serializer implements NodeSerializer<ClanMembersCatalog> {
 *         @Override
 *         public ClanMembersCatalog deserialize(Node node, Type type) {
 *             return new ClanMembersCatalog();
 *         }
 *     }
 * }
 *
 * // Registration inside the addon's onEnable:
 * api.catalogs().register("clanMembers",
 *         ClanMembersCatalog.class,
 *         new ClanMembersCatalog.Serializer(),
 *         this);
 * }</pre>
 *
 * <h2>Menu usage</h2>
 *
 * <pre>{@code
 * type: generatedMenu
 * catalog: { type: clanMembers }
 * matrix: [ "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa" ]
 * items {
 *   a: {
 *     material: PLAYER_HEAD
 *     name: "%name%"
 *     lore: [ "Rank: %rank%" ]
 *   }
 * }
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * {@link #snapshot(Player, Menu)} is called on the main server thread on every
 * render / page change. Keep the implementation cheap &mdash; return a cached
 * snapshot and refresh it asynchronously if the source data is expensive to
 * compute.
 *
 * @param <T> the element type produced by this catalog
 *
 * @see ValueExtractor
 * @see TypeRegistry
 * @see AbstractMenusApi#catalogs()
 */
public interface Catalog<T> {

    /**
     * Snapshot of the collection to render for {@code player} in {@code menu}.
     *
     * <p>Called every time the menu repaints &mdash; on open, on page change,
     * and on every refresh tick &mdash; so implementations should return a
     * cheap view over pre-computed state.
     *
     * @param player the menu viewer; never {@code null} and always online
     * @param menu   the menu this catalog is attached to; never {@code null}
     * @return the collection to paginate across the menu; never {@code null},
     *         but may be empty
     *
     * @implNote Runs on the main server thread. Do not block on IO &mdash;
     *           pre-compute and refresh asynchronously.
     */
    Collection<T> snapshot(Player player, Menu menu);

    /**
     * Extractor used to resolve placeholders against each element produced by
     * {@link #snapshot(Player, Menu)} when rendering the layout's item
     * template.
     *
     * @return the value extractor; may be {@code null} if this catalog's
     *         elements don't contribute any placeholders
     */
    ValueExtractor extractor();

}
