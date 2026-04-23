package ru.abstractmenus.addon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.abstractmenus.api.MenuExtension;

/**
 * Mutable container representing an AM-loaded addon. Holds the parsed
 * {@link AddonConf}, the live {@link MenuExtension} instance, its
 * {@link AddonClassLoader}, and lifecycle status.
 *
 * <p>Instances are created by {@code AddonManager} during discovery and
 * transition through enable → disable / failed states.
 */
@RequiredArgsConstructor
@Getter
public final class LoadedAddon {


    private final AddonConf conf;
    private final AddonClassLoader classLoader;
    @Setter
    private MenuExtension extension;     // null until onLoad completes
    private AddonStatus status = AddonStatus.PENDING;
    private Throwable error;             // non-null iff status == FAILED

    public void markEnabled() {
        this.status = AddonStatus.ENABLED;
        this.error = null;
    }

    public void markDisabled() {
        this.status = AddonStatus.DISABLED;
        this.error = null;
    }

    public void markFailed(Throwable t) {
        this.status = AddonStatus.FAILED;
        this.error = t;
    }
}
