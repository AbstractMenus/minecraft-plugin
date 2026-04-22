package ru.abstractmenus.core;

/**
 * HOCON-visible keys for core item properties. Centralised so that the
 * registration site ({@link CoreItemPropsBundle}) and consumers elsewhere
 * in the plugin reference the same symbol rather than duplicating string
 * literals.
 *
 * <p>Only those keys that are referenced by name outside
 * {@link CoreItemPropsBundle} need a constant here. The rest stay as
 * literals at their single point of use.
 */
public final class CoreItemPropertyKeys {

    private CoreItemPropertyKeys() {
    }

    /** The {@code bindings} property — referenced by {@code actionClear}. */
    public static final String BINDINGS = "bindings";
}
