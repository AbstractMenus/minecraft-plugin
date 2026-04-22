package ru.abstractmenus.api.variables;

/**
 * Variable data. Variables are immutable.
 * If you need to change and save it again, you need to clone it first.
 */
public interface Var {

    /**
     * Name of this variable
     * @return Name of this variable
     */
    String name();

    /**
     * Raw value of variable. Values always stored in string format
     * @return Raw value of this variable
     */
    String value();

    /**
     * Expiry time of this variable, if specified.
     * This time can be compared, using System.currentTimeMillis()
     * If variable has no expiry time, it will return 0
     * @return Expiry time
     */
    long expiry();

    /**
     * Has this variable expiry time
     * @return Try if variable has expiry time or false otherwise
     */
    boolean hasExpiry();

    /**
     * Is this variable expired
     * @return true if variable expired, false otherwise
     */
    boolean isExpired();

    /**
     * Parse value as boolean
     * Raw value will be parsed as true, if it equals to "true" or "1"
     * @return Boolean value, parsed from raw string
     */
    boolean boolValue();

    /**
     * Parse value as integer
     * @return Integer value, parsed from raw string
     * @throws NumberFormatException if raw value cannot be parsed as integer
     */
    int intValue() throws NumberFormatException;

    /**
     * Parse value as long
     * @return Long value, parsed from raw string
     * @throws NumberFormatException if raw value cannot be parsed as long
     */
    long longValue() throws NumberFormatException;

    /**
     * Parse value as float
     * @return Float value, parsed from raw string
     * @throws NumberFormatException if raw value cannot be parsed as float
     */
    float floatValue() throws NumberFormatException;

    /**
     * Parse value as double
     * @return Double value, parsed from raw string
     * @throws NumberFormatException if raw value cannot be parsed as double
     */
    double doubleValue() throws NumberFormatException;

    /**
     * Convert this value to builder with current values
     * @return New builder with current var values
     */
    VarBuilder toBuilder();

}
