package ru.abstractmenus.api.variables;

/**
 * Builder for variable
 */
public interface VarBuilder {

    /**
     * Get current name
     * @return Current name
     */
    String name();

    /**
     * Get current value
     * @return Current value
     */
    String value();

    /**
     * Get current expiry time
     * @return Current expiry time
     */
    long expiry();

    /**
     * Set expiry time to variable
     * @param name Variable name. Name should contain only Latin chars and special symbol `_`
     * @return Builder instance
     */
    VarBuilder name(String name);

    /**
     * Set expiry time to variable
     * @param value Variable value
     * @return Builder instance
     */
    VarBuilder value(String value);

    /**
     * Set expiry time to variable
     * @param expiry Expiry time in millis. Use System.currentTimeMillis() to
     *               get current time and add required millis to variable lifetime
     * @return Builder instance
     */
    VarBuilder expiry(long expiry);

    /**
     * Build new variable
     * @return New variable
     */
    Var build();

}
