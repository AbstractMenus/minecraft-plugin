package ru.abstractmenus.addon;

/**
 * Bukkit-plugin presence probe used by {@link AddonManager} to verify
 * an addon's {@code pluginDependencies} / {@code pluginSoftDependencies}
 * before instantiation.
 *
 * <p>Lifted to its own interface so the production class doesn't carry
 * a nullable {@code plugin} field that every caller has to guard - the
 * test mode injects {@link #ALL_PRESENT} which always reports true.
 */
@FunctionalInterface
interface PluginDepChecker {

    /** @return true if a Bukkit plugin with this name is loaded and enabled. */
    boolean isPresent(String pluginName);

    /** Test-mode checker: every name reports as present. No Bukkit calls. */
    PluginDepChecker ALL_PRESENT = name -> true;
}
