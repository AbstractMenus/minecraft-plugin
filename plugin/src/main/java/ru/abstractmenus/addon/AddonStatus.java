package ru.abstractmenus.addon;

/** Lifecycle state of an addon as tracked by {@code AddonManager}. */
public enum AddonStatus {
    /** Addon discovered and parsed; not yet enabled. */
    PENDING,
    /** Enabled — {@code onEnable} completed without throwing. */
    ENABLED,
    /** Cleanly disabled — {@code onDisable} ran and registry entries were cleared. */
    DISABLED,
    /** Failed to load or enable — see {@link LoadedAddon#getError()} for the cause. */
    FAILED
}
