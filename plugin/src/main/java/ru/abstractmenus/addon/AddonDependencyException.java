package ru.abstractmenus.addon;

/** Raised when an addon declares a dependency that isn't satisfiable. */
public class AddonDependencyException extends RuntimeException {
    public AddonDependencyException(String message) { super(message); }
}
