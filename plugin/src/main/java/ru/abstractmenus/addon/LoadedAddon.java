package ru.abstractmenus.addon;

import ru.abstractmenus.api.MenuExtension;

/**
 * Mutable container representing an AM-loaded addon. Holds the parsed
 * {@link AddonConf}, the live {@link MenuExtension} instance, its
 * {@link AddonClassLoader}, and lifecycle status.
 *
 * <p>Instances are created by {@code AddonManager} during discovery and
 * transition through enable → disable / failed states.
 */
public final class LoadedAddon {

    private final AddonConf conf;
    private final AddonClassLoader classLoader;
    private MenuExtension extension;     // null until onLoad completes
    private AddonStatus status = AddonStatus.PENDING;
    private Throwable error;             // non-null iff status == FAILED

    public LoadedAddon(AddonConf conf, AddonClassLoader classLoader) {
        this.conf = conf;
        this.classLoader = classLoader;
    }

    public AddonConf        conf()        { return conf; }
    public AddonClassLoader classLoader() { return classLoader; }
    public MenuExtension    extension()   { return extension; }
    public AddonStatus      status()      { return status; }
    public Throwable        error()       { return error; }

    public void setExtension(MenuExtension e) { this.extension = e; }
    public void markEnabled()                 { this.status = AddonStatus.ENABLED;  this.error = null; }
    public void markDisabled()                { this.status = AddonStatus.DISABLED; this.error = null; }
    public void markFailed(Throwable t)       { this.status = AddonStatus.FAILED;   this.error = t; }
}
