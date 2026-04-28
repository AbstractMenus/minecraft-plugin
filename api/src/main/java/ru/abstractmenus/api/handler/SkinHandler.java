package ru.abstractmenus.api.handler;

import org.bukkit.entity.Player;

/**
 * Handler for player skin operations invoked by skin-related menu actions.
 *
 * <p>A {@code SkinHandler} adapts a specific skin backend (SkinsRestorer,
 * direct NMS {@code GameProfile} manipulation, &hellip;) to the contract
 * AbstractMenus calls when a menu declares a skin-aware element:
 *
 * <ul>
 *   <li>the {@code setSkin} action calls
 *       {@link #setSkin(Player, String, String)} with a Mojang-style texture
 *       and signature pair;</li>
 *   <li>the {@code resetSkin} action calls {@link #resetSkin(Player)} to
 *       restore the player's original Mojang skin.</li>
 * </ul>
 *
 * <p>Unlike the other handlers, there is no matching rule section &mdash;
 * skins are applied, not queried, from menus.
 *
 * <h2>Registration</h2>
 *
 * Multiple handlers may coexist &mdash; the highest-priority one is picked
 * when a menu does not name a provider explicitly. Register yours via
 * {@link ru.abstractmenus.api.ProviderSection#register} inside your
 * addon's {@link ru.abstractmenus.api.MenuExtension#onEnable}.
 *
 * <p>When no skin handler is registered, {@code setSkin} and {@code resetSkin}
 * actions become no-ops &mdash; skin mutation requires packet-level access
 * that AbstractMenus does not ship with by default.
 *
 * <h2>Example &mdash; bridging SkinsRestorer</h2>
 *
 * SkinsRestorer exposes a high-level facade that handles player re-spawn,
 * packet re-sending, and persistence across sessions:
 *
 * <pre>{@code
 * public final class SkinsRestorerSkins implements SkinHandler {
 *
 *     private final SkinsRestorer sr;
 *
 *     public SkinsRestorerSkins(SkinsRestorer sr) { this.sr = sr; }
 *
 *     @Override
 *     public void setSkin(Player p, String texture, String signature) {
 *         SkinProperty prop = SkinProperty.of(texture, signature);
 *         sr.getPlayerStorage().setSkinOfPlayer(p.getUniqueId(), prop);
 *         sr.getSkinApplier(Player.class).applySkin(p, prop);
 *     }
 *
 *     @Override
 *     public void resetSkin(Player p) {
 *         sr.getPlayerStorage().removeSkinOfPlayer(p.getUniqueId());
 *         sr.getSkinApplier(Player.class).applySkin(p); // reapply Mojang skin
 *     }
 * }
 * }</pre>
 *
 * <p>An alternative impl may talk to NMS directly by mutating the player's
 * {@code GameProfile} properties and sending
 * {@code PlayerInfoUpdatePacket}/respawn packets &mdash; but SkinsRestorer is
 * the canonical provider because it handles the packet dance correctly.
 *
 * <h2>Menu usage</h2>
 *
 * Once a handler is registered, menus can swap skins on click:
 *
 * <pre>{@code
 * items {
 *   pirate {
 *     material: PLAYER_HEAD
 *     name: "&ePirate skin"
 *     actions {
 *       click: [
 *         { type: setSkin, texture: "ewogICJ0aW1lc3RhbXA...", signature: "..." }
 *       ]
 *     }
 *   }
 *   reset {
 *     material: BARRIER
 *     name: "&cReset skin"
 *     actions { click: [ { type: resetSkin } ] }
 *   }
 * }
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * All methods are called on the main server thread in response to inventory
 * events. Applying a skin typically involves a brief packet burst
 * (remove-player, add-player, respawn); SkinsRestorer already schedules this
 * correctly. Implementations that hit an external texture-fetching service
 * (e.g. mineskin.org) <strong>must</strong> do the HTTP call asynchronously
 * and apply on the main thread &mdash; blocking here freezes every online
 * player.
 *
 * @see ru.abstractmenus.api.ProviderSection#register
 * @see EconomyHandler
 */
public interface SkinHandler {

    /**
     * Applies the given texture/signature pair as the player's active skin.
     * Backs the {@code setSkin} menu action.
     *
     * <p>The {@code texture} is the Base64-encoded JSON payload as returned
     * by Mojang's session server (the {@code value} field of the
     * {@code textures} property on a {@code GameProfile}). The
     * {@code signature} is the matching Yggdrasil signature &mdash; without
     * it, vanilla clients will reject the skin.
     *
     * @param player    the player whose skin is being set; never {@code null},
     *                  always online
     * @param texture   the Base64-encoded texture payload; never {@code null},
     *                  never empty
     * @param signature the matching Yggdrasil signature; never {@code null},
     *                  never empty
     *
     * @implNote Typical flow: update the player's {@code GameProfile}
     *           properties, send a {@code PlayerInfoUpdatePacket} with
     *           {@code REMOVE_PLAYER} then {@code ADD_PLAYER} actions to
     *           every viewer, and respawn the target player so their own
     *           client re-renders. SkinsRestorer does this for you.
     */
    void setSkin(Player player, String texture, String signature);

    /**
     * Resets the player's skin to their original Mojang-hosted skin. Backs
     * the {@code resetSkin} menu action.
     *
     * <p>If the player never had a custom skin applied the call is a no-op.
     *
     * @param player the player whose skin is being reset; never {@code null},
     *               always online
     *
     * @implNote Implementations that cache applied skins should clear the
     *           entry for this player so a subsequent {@code setSkin} call
     *           reflects the new value rather than a stale cache.
     */
    void resetSkin(Player player);

}
