package ru.abstractmenus.addon;

/** Unchecked exception for malformed or incomplete {@code addon.conf} files. */
public class AddonConfParseException extends RuntimeException {

    public AddonConfParseException(String message) {
        super(message);
    }

    public AddonConfParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
