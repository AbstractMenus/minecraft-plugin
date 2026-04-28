package ru.abstractmenus.api;

import java.util.Collection;
import java.util.Set;

/**
 * One section of the {@link ProviderRegistry}, holding registered handlers
 * of a single provider type (economy, permissions, levels, placeholders,
 * or skins).
 *
 * <p>Each handler is registered under a string id (e.g. {@code "vault"},
 * {@code "playerpoints"}), with a priority for auto-resolution and an
 * owning {@link MenuExtension} for cleanup. Sections are mirrors of one
 * another structurally; {@code ProviderRegistry.economy()} returns a
 * {@code ProviderSection<EconomyHandler>}, and so on. There is no per-type
 * boilerplate on the registry interface.
 *
 * <h2>Resolution</h2>
 *
 * <ul>
 *   <li>{@link #resolve()} returns the highest-priority registered handler,
 *       overridden by the {@code config.conf providers.<kind>} value if it
 *       names a registered handler. Returns {@code null} if the section is
 *       empty.</li>
 *   <li>{@link #resolve(String)} returns the explicitly-named handler, or
 *       {@code null} if no handler with that id is registered.</li>
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * // Register a custom economy provider in onEnable:
 * api.providers().economy().register(
 *     "playerpoints", new PlayerPointsEconomy(pp), 100, this);
 *
 * // From an action, ask for the configured/auto-resolved economy:
 * EconomyHandler eco = api.providers().economy().resolve();
 *
 * // Or for a specific provider by id:
 * EconomyHandler vault = api.providers().economy().resolve("vault");
 * }</pre>
 *
 * <h2>Thread-safety</h2>
 *
 * Implementations are expected to be thread-safe for concurrent reads and
 * writes - addon enable/disable can race against extractor lookups on Folia
 * since regions run independently. The reference implementation
 * synchronises on the section instance and resolves with-default
 * atomically (so a concurrent unregister cannot leave the resolver
 * pointing at a freed handler).
 */
public interface ProviderSection<T> {

    /**
     * Register a handler under {@code id}. If an entry already exists for
     * {@code id}, it is replaced. {@code priority} controls auto-resolve
     * order: highest wins. {@code owner} is the {@link MenuExtension} that
     * registered this entry; AbstractMenus' addon manager uses it for
     * cleanup on disable / reload.
     *
     * @param id       case-insensitive identifier (e.g. {@code "vault"})
     * @param handler  the handler instance
     * @param priority higher wins in {@link #resolve()} when no config
     *                 default is set; core providers register at 50, addons
     *                 typically at 100
     * @param owner    the registering extension; used for cleanup
     */
    void register(String id, T handler, int priority, MenuExtension owner);

    /**
     * Resolve the handler that should serve "default" lookups. Tries the
     * configured id from {@code config.conf providers.<kind>} first; if
     * missing or set to {@code "auto"}, falls back to the highest-priority
     * registered handler.
     *
     * @return a registered handler, or {@code null} if the section is empty
     */
    T resolve();

    /**
     * Resolve a specific handler by id.
     *
     * @param id case-insensitive identifier
     * @return the registered handler, or {@code null} if not registered
     */
    T resolve(String id);

    /** All registered handlers, in registration order. Read-only snapshot. */
    Collection<T> all();

    /**
     * All registered ids, lowercased and in registration order. Useful for
     * "did you mean" or "unknown id, registered: [...]" error messages
     * where you want to surface the actual configurable names rather than
     * impl class names.
     *
     * @return read-only snapshot of registered ids
     */
    Set<String> ids();

    /**
     * @param id case-insensitive identifier
     * @return whether a handler with this id is registered
     */
    boolean has(String id);
}
