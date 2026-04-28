package ru.abstractmenus.api;

/**
 * Thin static facade over the plugin's {@link java.util.logging.Logger} for
 * user-visible log output.
 *
 * <p>Used by both core and addons so log lines end up tagged with the
 * AbstractMenus plugin prefix in the server console regardless of which module
 * produced them. The underlying logger is injected once, from the plugin's
 * {@code onEnable}, via {@link #set(java.util.logging.Logger)}.
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * // Once, inside AbstractMenus#onEnable:
 * Logger.set(getLogger());
 *
 * // Anywhere in core or an addon:
 * Logger.info("Menu loaded: " + menu.getName());
 * Logger.warning("Unknown material in " + menu.getName() + ": " + raw);
 * Logger.severe("Failed to load menu " + menu.getName());
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * The backing {@link java.util.logging.Logger} is thread-safe &mdash; calls
 * from async tasks are fine. {@link #set(java.util.logging.Logger)} is expected
 * to run once on the main server thread before any other method is called.
 *
 * @see MenuExtension
 */
public final class Logger {

    private static java.util.logging.Logger logger;

    private Logger(){}

    /**
     * Install the backing JUL logger. Called once by AbstractMenus core during
     * plugin {@code onEnable}.
     *
     * <p>Set-once: subsequent calls are silently skipped so an addon cannot
     * replace the logger to silence or capture core's output. The skip
     * (rather than throw) keeps Bukkit {@code /reload} working - on reload
     * core's {@code onEnable} runs again and re-invokes this method, which
     * is now a no-op.
     *
     * @param log the JUL logger to delegate to; never {@code null}
     * @throws NullPointerException if {@code log} is null
     */
    public static void set(java.util.logging.Logger log){
        if (log == null) throw new NullPointerException("logger");
        if (logger != null) return;
        logger = log;
    }

    /**
     * Log an {@code INFO}-level message.
     *
     * @param message the message to log; may contain any characters valid in
     *                a server console line
     */
    public static void info(String message){
        logger.info(message);
    }

    /**
     * Log a {@code WARNING}-level message.
     *
     * <p>Use for recoverable configuration mistakes (unknown material, missing
     * placeholder, fallback applied) &mdash; not for programmer errors.
     *
     * @param message the message to log
     */
    public static void warning(String message){
        logger.warning(message);
    }

    /**
     * Log a {@code SEVERE}-level (error) message.
     *
     * <p>Use for failures that prevent a menu / action / rule from functioning
     * &mdash; IO errors, schema violations, unhandled exceptions.
     *
     * @param message the message to log
     */
    public static void severe(String message){
        logger.severe(message);
    }

}
