package ru.abstractmenus.api.variables;

/**
 * CRUD gateway for AbstractMenus variables &mdash; the runtime behind
 * {@code %var_%} / {@code %varp_%} placeholders and the {@code /var} /
 * {@code /varp} commands.
 *
 * <p>The manager maintains an in-memory cache keyed by a compound
 * {@code scope:name} identifier:
 *
 * <ul>
 *   <li><strong>Global</strong> variables live under a synthetic scope and are
 *       visible to every player.</li>
 *   <li><strong>Personal</strong> variables live under the owner's username;
 *       two players may hold variables with the same {@code name} without
 *       collision.</li>
 * </ul>
 *
 * <p>Variables are persisted to a SQLite database (per-server) and, when
 * cross-server sync is enabled in {@code config.conf}, broadcast over BungeeCord
 * to keep sibling servers in lock-step. Expired variables are swept from the
 * cache once per second by a background task.
 *
 * <h2>Obtaining the manager</h2>
 *
 * <pre>{@code
 * VariableManager vm = AbstractMenusApi.get().variables();
 * }</pre>
 *
 * <h2>Example &mdash; daily-reward gate</h2>
 *
 * <pre>{@code
 * Var claimed = vm.getPersonal(player.getName(), "dailyReward");
 * if (claimed == null || claimed.isExpired()) {
 *     economy.giveBalance(player, 500);
 *
 *     Var marker = vm.createBuilder()
 *             .name("dailyReward")
 *             .value("claimed")
 *             .expiry(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24))
 *             .build();
 *
 *     vm.savePersonal(player.getName(), marker);
 * }
 * }</pre>
 *
 * <h2>Menu usage</h2>
 *
 * The same state drives rules and placeholder-backed item text:
 *
 * <pre>{@code
 * items {
 *   daily {
 *     material: CHEST
 *     name: "&6Daily reward"
 *     rules: [
 *       { type: notVarpEquals, name: dailyReward, value: claimed }
 *     ]
 *     actions.click: [
 *       { type: giveMoney,  amount: 500 }
 *       { type: varpSet,    name: dailyReward, value: claimed, expiry: 86400000 }
 *     ]
 *   }
 * }
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * Reads ({@link #getGlobal}, {@link #getPersonal}) hit the in-memory cache and
 * are safe from any thread. Writes ({@link #saveGlobal saveGlobal},
 * {@link #savePersonal savePersonal}, {@link #deleteGlobal deleteGlobal},
 * {@link #deletePersonal deletePersonal}) update the cache synchronously but
 * issue a SQLite write and, if enabled, a BungeeCord forward as part of the
 * call &mdash; both may block. Prefer calling these from an async scheduler
 * task when bulk-updating variables.
 *
 * @see Var
 * @see VarBuilder
 */
public interface VariableManager {

    /**
     * Look up a global variable by name.
     *
     * @param name the variable name; compared case-insensitively
     * @return the cached {@link Var}, or {@code null} if no variable is
     *         registered under that name (or it has expired and been swept)
     */
    Var getGlobal(String name);

    /**
     * Look up a personal variable owned by {@code username}.
     *
     * @param username the owning player's name; compared case-insensitively
     * @param name     the variable name; compared case-insensitively
     * @return the cached {@link Var}, or {@code null} if the player has no
     *         such variable (or it has expired and been swept)
     */
    Var getPersonal(String username, String name);

    /**
     * Persist a global variable.
     *
     * <p>If a variable with the same name already exists in the cache and
     * {@code replace} is {@code false}, the call is a no-op. Expired incoming
     * variables are silently dropped.
     *
     * @param var     the variable to persist; never {@code null}
     * @param replace whether to overwrite an existing entry with the same name
     * @apiNote Triggers a SQLite write on the calling thread and, when
     *          cross-server sync is enabled, a {@code SyncVar} plugin message
     *          to BungeeCord. Not suitable for tight main-thread loops.
     */
    void saveGlobal(Var var, boolean replace);

    /**
     * Convenience wrapper that always overwrites an existing entry.
     *
     * @param var the variable to persist; never {@code null}
     * @see #saveGlobal(Var, boolean)
     */
    default void saveGlobal(Var var) {
        saveGlobal(var, true);
    }

    /**
     * Persist a personal variable owned by {@code username}.
     *
     * <p>If a variable with the same name already exists for the player and
     * {@code replace} is {@code false}, the call is a no-op. Expired incoming
     * variables are silently dropped.
     *
     * @param username the owning player's name; never {@code null}
     * @param var      the variable to persist; never {@code null}
     * @param replace  whether to overwrite an existing entry with the same
     *                 name
     * @apiNote Triggers a SQLite write on the calling thread and, when
     *          cross-server sync is enabled, a {@code SyncVar} plugin message
     *          to BungeeCord. Not suitable for tight main-thread loops.
     */
    void savePersonal(String username, Var var, boolean replace);

    /**
     * Convenience wrapper that always overwrites an existing entry.
     *
     * @param username the owning player's name; never {@code null}
     * @param var      the variable to persist; never {@code null}
     * @see #savePersonal(String, Var, boolean)
     */
    default void savePersonal(String username, Var var) {
        savePersonal(username, var, true);
    }

    /**
     * Delete a global variable.
     *
     * <p>Removes the entry from the cache, deletes the row from SQLite and, if
     * cross-server sync is enabled, broadcasts the deletion to sibling
     * servers. Unknown names are silently ignored.
     *
     * @param name the variable name; compared case-insensitively
     * @apiNote Performs IO on the calling thread &mdash; consider scheduling
     *          off the main thread when deleting many variables in a row.
     */
    void deleteGlobal(String name);

    /**
     * Delete a personal variable owned by {@code username}.
     *
     * <p>Removes the entry from the cache, deletes the row from SQLite and, if
     * cross-server sync is enabled, broadcasts the deletion to sibling
     * servers. Unknown combinations are silently ignored.
     *
     * @param username the owning player's name; compared case-insensitively
     * @param name     the variable name; compared case-insensitively
     * @apiNote Performs IO on the calling thread &mdash; consider scheduling
     *          off the main thread when deleting many variables in a row.
     */
    void deletePersonal(String username, String name);

    /**
     * Create an empty {@link VarBuilder} for a brand-new variable. To derive
     * one from an existing variable instead, call {@link Var#toBuilder()}.
     *
     * @return a fresh builder; never {@code null}
     */
    VarBuilder createBuilder();

}
