package ru.abstractmenus.api.variables;

/**
 * Immutable snapshot of a single AbstractMenus variable &mdash; a named string
 * value with an optional expiry timestamp.
 *
 * <p>Variables come in two flavours, distinguished only by how they are stored
 * through {@link VariableManager}:
 *
 * <ul>
 *   <li><strong>Global</strong> &mdash; server-wide, shared across players;
 *       exposed to menus as {@code %var_<name>%} and manipulated by the
 *       {@code /var} command.</li>
 *   <li><strong>Personal</strong> &mdash; scoped to a single player (by
 *       username); exposed as {@code %varp_<name>%} and manipulated by the
 *       {@code /varp} command and by the {@code varpSet} / {@code varpAdd}
 *       menu actions.</li>
 * </ul>
 *
 * <p>The value is always kept as a {@code String}; the numeric accessors are
 * pure parse helpers and do not cache the parsed result. Instances are
 * immutable &mdash; to change a field, call {@link #toBuilder()}, mutate the
 * returned {@link VarBuilder}, and pass the rebuilt {@code Var} back to
 * {@link VariableManager#saveGlobal(Var)} /
 * {@link VariableManager#savePersonal(String, Var)}.
 *
 * <h2>Example &mdash; increment a personal counter</h2>
 *
 * <pre>{@code
 * VariableManager vm = AbstractMenusApi.get().variables();
 *
 * Var counter = vm.getPersonal(player.getName(), "kills");
 * long next = (counter == null) ? 1L : counter.longValue() + 1L;
 *
 * Var updated = (counter != null ? counter.toBuilder() : vm.createBuilder())
 *         .name("kills")
 *         .value(Long.toString(next))
 *         .build();
 *
 * vm.savePersonal(player.getName(), updated);
 * }</pre>
 *
 * <h2>Menu usage</h2>
 *
 * Placeholders resolve the latest cached value on every lookup:
 *
 * <pre>{@code
 * items {
 *   coin {
 *     material: GOLD_NUGGET
 *     name: "&eCoins: %varp_coins%"
 *     actions.click: [
 *       { type: varpAdd, name: coins, value: 1 }
 *     ]
 *   }
 * }
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * Instances are safe to read from any thread &mdash; every field is final and
 * the expiry check is a plain {@code System.currentTimeMillis()} comparison.
 * Writes go through {@link VariableManager}; see that interface for the
 * threading contract around persistence.
 *
 * @see VariableManager
 * @see VarBuilder
 */
public interface Var {

    /**
     * Variable name as registered. Names are case-insensitive at the storage
     * layer but the returned string preserves the casing used when the
     * variable was built.
     *
     * @return the variable name; never {@code null}
     */
    String name();

    /**
     * Raw string value. Numeric variables are still returned as strings &mdash;
     * use {@link #intValue()}, {@link #longValue()}, {@link #floatValue()} or
     * {@link #doubleValue()} to parse on demand.
     *
     * @return the raw value; never {@code null}
     */
    String value();

    /**
     * Absolute expiry timestamp in milliseconds since the Unix epoch, directly
     * comparable with {@link System#currentTimeMillis()}. A return of
     * {@code 0} signals "no expiry" &mdash; see {@link #hasExpiry()}.
     *
     * @return the expiry timestamp, or {@code 0} if the variable never expires
     */
    long expiry();

    /**
     * Whether this variable was built with an expiry timestamp.
     *
     * @return {@code true} if {@link #expiry()} is strictly positive,
     *         {@code false} otherwise
     */
    boolean hasExpiry();

    /**
     * Whether this variable has already expired relative to the current wall
     * clock. Expired variables are evicted from the manager's cache by a
     * periodic sweep (once per second) and are not written back to disk.
     *
     * @return {@code true} if {@link #hasExpiry()} is {@code true} and the
     *         expiry timestamp is in the past or present; {@code false}
     *         otherwise
     */
    boolean isExpired();

    /**
     * Parse the raw value as a boolean. The raw value is considered
     * {@code true} if, after lower-casing, it equals {@code "true"} or
     * {@code "1"}; any other value &mdash; including {@code null}-like strings
     * &mdash; is {@code false}.
     *
     * @return the parsed boolean value
     */
    boolean boolValue();

    /**
     * Parse the raw value as a signed 32-bit integer.
     *
     * @return the parsed integer
     * @throws NumberFormatException if the raw value is not a valid
     *                               {@code int} literal
     */
    int intValue() throws NumberFormatException;

    /**
     * Parse the raw value as a signed 64-bit integer.
     *
     * @return the parsed long
     * @throws NumberFormatException if the raw value is not a valid
     *                               {@code long} literal
     */
    long longValue() throws NumberFormatException;

    /**
     * Parse the raw value as a single-precision float.
     *
     * @return the parsed float
     * @throws NumberFormatException if the raw value is not a valid
     *                               {@code float} literal
     */
    float floatValue() throws NumberFormatException;

    /**
     * Parse the raw value as a double-precision float.
     *
     * @return the parsed double
     * @throws NumberFormatException if the raw value is not a valid
     *                               {@code double} literal
     */
    double doubleValue() throws NumberFormatException;

    /**
     * Return a fresh {@link VarBuilder} pre-populated with this variable's
     * fields. Modifying the builder does not affect this instance.
     *
     * @return a new builder seeded with {@link #name()}, {@link #value()} and
     *         {@link #expiry()}
     */
    VarBuilder toBuilder();

}
