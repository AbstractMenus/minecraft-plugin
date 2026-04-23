package ru.abstractmenus.api.handler;

import org.bukkit.entity.Player;

/**
 * Handler for permission and group operations invoked by permission-related
 * menu actions and rules.
 *
 * <p>A {@code PermissionsHandler} adapts a specific permissions backend
 * (LuckPerms, PermissionsEx, GroupManager, &hellip;) to the contract
 * AbstractMenus calls when a menu declares a permission-aware element:
 *
 * <ul>
 *   <li>the {@code givePermission} / {@code removePermission} actions call
 *       {@link #addPermission(Player, String)} and
 *       {@link #removePermission(Player, String)};</li>
 *   <li>the {@code addGroup} / {@code removeGroup} actions call
 *       {@link #addGroup(Player, String)} and
 *       {@link #removeGroup(Player, String)};</li>
 *   <li>the {@code permission} rule calls
 *       {@link #hasPermission(Player, String)} and the {@code group} rule
 *       calls {@link #hasGroup(Player, String)} to decide whether the element
 *       is shown to the player.</li>
 * </ul>
 *
 * <h2>Registration</h2>
 *
 * Multiple handlers may coexist &mdash; the highest-priority one is picked
 * when a menu does not name a provider explicitly. Register yours via
 * {@link ru.abstractmenus.api.ProviderRegistry#registerPermissions} inside
 * your addon's {@link ru.abstractmenus.api.MenuExtension#onEnable}.
 *
 * <p>If no permissions handler is registered, permission nodes are checked
 * against Bukkit's in-memory permission set (persisted only as long as the
 * player session lives) and group operations become no-ops. Register a proper
 * LuckPerms-backed handler to persist grants across reloads.
 *
 * <h2>Example &mdash; bridging LuckPerms</h2>
 *
 * LuckPerms' {@code User} API exposes node-level mutation. The bridge loads
 * the user, mutates, and saves:
 *
 * <pre>{@code
 * public final class LuckPermsPermissions implements PermissionsHandler {
 *
 *     private final LuckPerms lp;
 *
 *     public LuckPermsPermissions(LuckPerms lp) { this.lp = lp; }
 *
 *     @Override
 *     public void addPermission(Player p, String permission) {
 *         lp.getUserManager().modifyUser(p.getUniqueId(), u ->
 *             u.data().add(Node.builder(permission).build()));
 *     }
 *
 *     @Override
 *     public void removePermission(Player p, String permission) {
 *         lp.getUserManager().modifyUser(p.getUniqueId(), u ->
 *             u.data().remove(Node.builder(permission).build()));
 *     }
 *
 *     @Override
 *     public boolean hasPermission(Player p, String permission) {
 *         return p.hasPermission(permission); // LuckPerms injects into Bukkit
 *     }
 *
 *     @Override
 *     public void addGroup(Player p, String group) {
 *         lp.getUserManager().modifyUser(p.getUniqueId(), u ->
 *             u.data().add(InheritanceNode.builder(group).build()));
 *     }
 *
 *     @Override
 *     public void removeGroup(Player p, String group) {
 *         lp.getUserManager().modifyUser(p.getUniqueId(), u ->
 *             u.data().remove(InheritanceNode.builder(group).build()));
 *     }
 *
 *     @Override
 *     public boolean hasGroup(Player p, String group) {
 *         User u = lp.getUserManager().getUser(p.getUniqueId());
 *         return u != null && u.getInheritedGroups(u.getQueryOptions())
 *                 .stream().anyMatch(g -> g.getName().equalsIgnoreCase(group));
 *     }
 * }
 * }</pre>
 *
 * <h2>Menu usage</h2>
 *
 * Once a handler is registered, menus use permission primitives without
 * caring which backing plugin is wired up:
 *
 * <pre>{@code
 * rules: [
 *   { type: permission, value: "myserver.vip" }
 * ]
 * actions {
 *   click: [
 *     { type: givePermission, value: "myserver.reward.claimed" }
 *     { type: addGroup,       value: "donor" }
 *   ]
 * }
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * All methods are called on the main server thread in response to inventory
 * events. Getters ({@link #hasPermission}, {@link #hasGroup}) must be cheap
 * &mdash; LuckPerms caches inheritance resolution, but custom backends should
 * avoid blocking IO here. Mutators ({@link #addPermission},
 * {@link #addGroup}, &hellip;) may schedule the persistence step
 * asynchronously provided the in-memory view updates before the next
 * {@code has*} call.
 *
 * @see ru.abstractmenus.api.ProviderRegistry#registerPermissions
 * @see EconomyHandler
 * @see LevelHandler
 */
public interface PermissionsHandler {

    /**
     * Grants {@code permission} to the player. Backs the
     * {@code givePermission} menu action.
     *
     * @param player     the player to grant; never {@code null}, always online
     * @param permission the permission node (e.g. {@code "myserver.vip"});
     *                   never {@code null}, non-empty
     *
     * @implNote If the backend persists asynchronously, ensure the in-memory
     *           view updates synchronously so that a follow-up
     *           {@link #hasPermission(Player, String)} call in the same tick
     *           returns {@code true}.
     */
    void addPermission(Player player, String permission);

    /**
     * Revokes {@code permission} from the player. Backs the
     * {@code removePermission} menu action.
     *
     * <p>If the player does not currently hold the permission the call is a
     * no-op &mdash; revocation of an absent node is not an error.
     *
     * @param player     the player to revoke; never {@code null}, always online
     * @param permission the permission node to remove; never {@code null},
     *                   non-empty
     */
    void removePermission(Player player, String permission);

    /**
     * Returns {@code true} if the player holds {@code permission}. Backs the
     * {@code permission} menu rule.
     *
     * <p>This is a pure predicate &mdash; implementations must not mutate
     * state. Inherited permissions (via groups or wildcards) count as
     * {@code true}.
     *
     * @param player     the player being checked; never {@code null}, always
     *                   online
     * @param permission the permission node to test; never {@code null},
     *                   non-empty
     * @return {@code true} if the player holds the permission directly or by
     *         inheritance, {@code false} otherwise
     *
     * @implNote Callers assume this is cheap &mdash; avoid blocking IO.
     *           Delegate to {@link Player#hasPermission(String)} when the
     *           backend injects into Bukkit's permission system (LuckPerms,
     *           PermissionsEx).
     */
    boolean hasPermission(Player player, String permission);

    /**
     * Adds the player to the named group. Backs the {@code addGroup} menu
     * action.
     *
     * @param player the player to add; never {@code null}, always online
     * @param group  the group name; never {@code null}, non-empty, case
     *               sensitivity is backend-defined
     *
     * @implNote If the backend does not model groups (e.g. a permissions-only
     *           provider), treat the call as a no-op rather than throwing.
     */
    void addGroup(Player player, String group);

    /**
     * Removes the player from the named group. Backs the {@code removeGroup}
     * menu action.
     *
     * <p>If the player is not currently in the group the call is a no-op.
     *
     * @param player the player to remove; never {@code null}, always online
     * @param group  the group name; never {@code null}, non-empty
     */
    void removeGroup(Player player, String group);

    /**
     * Returns {@code true} if the player is a member (direct or inherited) of
     * the named group. Backs the {@code group} menu rule.
     *
     * <p>This is a pure predicate &mdash; implementations must not mutate
     * state.
     *
     * @param player the player being checked; never {@code null}, always
     *               online
     * @param group  the group name to test; never {@code null}, non-empty
     * @return {@code true} if the player is a member of the group directly or
     *         via group inheritance, {@code false} otherwise &mdash; including
     *         when the backend does not recognise the group
     *
     * @implNote Callers assume this is cheap &mdash; avoid blocking IO.
     */
    boolean hasGroup(Player player, String group);

}
