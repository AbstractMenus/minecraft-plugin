package ru.abstractmenus.api;

/**
 * Contract an AbstractMenus addon implements to register custom types
 * (actions, rules, item properties, activators, catalogs) at runtime.
 *
 * <p>Two paths for delivery:
 *
 * <ul>
 *   <li><strong>Plugin-as-addon (Path 1)</strong> &mdash; a standard Bukkit plugin
 *       that {@code depend}s on AbstractMenus, implements {@code MenuExtension},
 *       and looks up the running API via {@link AbstractMenusApi#get()} inside
 *       its own {@code onEnable()}. Use this when your addon integrates other
 *       Bukkit plugins (PlayerPoints, WorldGuard, &hellip;) and needs Bukkit's
 *       native plugin dependency ordering.</li>
 *
 *   <li><strong>AM-loaded addon (Path 2)</strong> &mdash; a lightweight jar with an
 *       {@code addon.conf} placed in {@code plugins/AbstractMenus/addons/}.
 *       AbstractMenus loads it through an isolated classloader and calls the
 *       lifecycle methods below. Use this for pure type extensions with no
 *       cross-plugin integration. (Shipped in Phase B.2.)</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 *
 * <ol>
 *   <li>{@link #onLoad(AbstractMenusApi)} &mdash; invoked for every extension
 *       before any {@code onEnable} runs. Use to perform ordering-independent
 *       setup. Do <em>not</em> register types here &mdash; other extensions
 *       may not be loaded yet.</li>
 *   <li>{@link #onEnable(AbstractMenusApi)} &mdash; invoked in dependency
 *       order (DAG sort over {@code addonDependencies} from addon.conf).
 *       Register types / providers here.</li>
 *   <li>{@link #onDisable(AbstractMenusApi)} &mdash; invoked on server
 *       shutdown or {@code /am addons reload}. Release resources (Bukkit
 *       listeners, scheduler tasks). Types registered through
 *       {@code api.*()} registries are auto-unregistered by AbstractMenus
 *       &mdash; you don't need to undo those manually.</li>
 * </ol>
 *
 * <h2>Thread safety</h2>
 *
 * All lifecycle methods are called on the main server thread. Implementations
 * are synchronous; offload blocking work to a scheduler.
 *
 * @see AbstractMenusApi
 * @see TypeRegistry
 */
public interface MenuExtension {

    /**
     * Pre-enable hook &mdash; called for every extension before any
     * extension's {@link #onEnable(AbstractMenusApi)} runs. Default is no-op.
     *
     * @param api the running AbstractMenus API
     */
    default void onLoad(AbstractMenusApi api) {
    }

    /**
     * Register types (and in Phase C, providers) against {@code api}. Called
     * in dependency order &mdash; extensions listed in this one's
     * {@code addonDependencies} are already enabled when this runs.
     *
     * @param api the running AbstractMenus API
     */
    void onEnable(AbstractMenusApi api);

    /**
     * Release resources. Called on server shutdown or a reload of this
     * extension. Default is no-op.
     *
     * <p>Registry entries are cleaned up automatically &mdash; you only need
     * to unregister Bukkit listeners, cancel scheduler tasks, and close any
     * resources you opened yourself.
     *
     * @param api the running AbstractMenus API
     */
    default void onDisable(AbstractMenusApi api) {
    }

    /**
     * Display name. Used by {@code /am addons list} and log messages.
     *
     * <p>Default falls back to {@code getClass().getSimpleName()} so a
     * Path 1 plugin-as-addon that forgets to override does not render
     * as "null" in operator output. Path 2 implementations should
     * override this to match the {@code name} field in {@code addon.conf}.
     *
     * @return the extension display name; never {@code null} by default
     */
    default String name() {
        return getClass().getSimpleName();
    }

    /**
     * Extension version string. Used for diagnostics.
     *
     * <p>Default returns {@code "unknown"} so {@code /am addons list}
     * does not render "vnull". Path 1 plugins should override to return
     * their plugin.yml version; Path 2 implementations should match the
     * {@code version} field in {@code addon.conf}.
     *
     * @return the version; never {@code null} by default
     */
    default String version() {
        return "unknown";
    }

    /**
     * The AbstractMenus API version this extension was built against. Purely
     * diagnostic &mdash; logged on load and shown in {@code /am addons info}.
     * No compatibility enforcement is performed; if the current API is
     * incompatible, the extension will fail naturally on {@code onEnable}
     * with a clear stacktrace.
     *
     * @return the target API version string (e.g. {@code "2.0.0"}), or
     *         {@code null} if not tracked
     */
    default String targetApiVersion() {
        return null;
    }
}
