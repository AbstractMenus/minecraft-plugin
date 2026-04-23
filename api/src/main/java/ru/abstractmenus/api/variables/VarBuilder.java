package ru.abstractmenus.api.variables;

/**
 * Fluent builder for {@link Var} instances.
 *
 * <p>Obtain an instance one of two ways, depending on whether you are creating
 * a fresh variable or mutating an existing one:
 *
 * <ul>
 *   <li>{@link VariableManager#createBuilder()} &mdash; an empty builder for a
 *       brand-new variable.</li>
 *   <li>{@link Var#toBuilder()} &mdash; a builder pre-populated with the
 *       fields of an existing variable, as {@link Var} is immutable.</li>
 * </ul>
 *
 * <p>{@link #name(String)} and {@link #value(String)} are required &mdash;
 * {@link #build()} throws {@link NullPointerException} otherwise. Expiry is
 * optional and defaults to {@code 0} (never expires).
 *
 * <h2>Example &mdash; a 10-minute cooldown variable</h2>
 *
 * <pre>{@code
 * VariableManager vm = AbstractMenusApi.get().variables();
 *
 * Var cooldown = vm.createBuilder()
 *         .name("ability_cooldown")
 *         .value("active")
 *         .expiry(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10))
 *         .build();
 *
 * vm.savePersonal(player.getName(), cooldown);
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * Builders are <strong>not</strong> thread-safe &mdash; construct and complete
 * them on a single thread. The {@link Var} produced by {@link #build()} is
 * fully immutable and safe to share.
 *
 * @see Var
 * @see VariableManager
 */
public interface VarBuilder {

    /**
     * Current staged name.
     *
     * @return the name, or {@code null} if {@link #name(String)} has not yet
     *         been called
     */
    String name();

    /**
     * Current staged value.
     *
     * @return the value, or {@code null} if {@link #value(String)} has not yet
     *         been called
     */
    String value();

    /**
     * Current staged expiry timestamp.
     *
     * @return the expiry in epoch millis, or {@code 0} if no expiry has been
     *         set
     */
    long expiry();

    /**
     * Set the variable name. Names may contain Latin letters, digits and the
     * underscore character ({@code _}); validation happens at the manager
     * layer when the variable is saved.
     *
     * @param name the variable name; must be non-{@code null} by
     *             {@link #build()} time
     * @return this builder, for chaining
     */
    VarBuilder name(String name);

    /**
     * Set the raw string value. Numeric values should be stringified via
     * {@code String.valueOf(&hellip;)} or {@link Long#toString(long)} &mdash;
     * the storage layer does not accept typed numbers.
     *
     * @param value the raw value; must be non-{@code null} by {@link #build()}
     *              time
     * @return this builder, for chaining
     */
    VarBuilder value(String value);

    /**
     * Set the absolute expiry timestamp in milliseconds since the Unix epoch.
     * Use {@code System.currentTimeMillis() + lifetimeMillis} to express a
     * relative lifetime; pass {@code 0} to disable expiry.
     *
     * @param expiry the expiry timestamp, or {@code 0} for no expiry
     * @return this builder, for chaining
     */
    VarBuilder expiry(long expiry);

    /**
     * Materialise the staged fields into an immutable {@link Var}.
     *
     * <p>As a convenience, values that parse as floating-point numbers with a
     * zero fractional part are normalised to their integer form (e.g.
     * {@code "5.0"} becomes {@code "5"}).
     *
     * @return a new {@link Var}; never {@code null}
     * @throws NullPointerException if {@link #name(String)} or
     *                              {@link #value(String)} was not called
     */
    Var build();

}
