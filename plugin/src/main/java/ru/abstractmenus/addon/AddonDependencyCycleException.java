package ru.abstractmenus.addon;

/** Raised when {@code addonDependencies} form a cycle. */
public class AddonDependencyCycleException extends AddonDependencyException {
    public AddonDependencyCycleException(String message) { super(message); }
}
