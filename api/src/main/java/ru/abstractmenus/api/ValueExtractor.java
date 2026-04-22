package ru.abstractmenus.api;

/**
 * Implementors of extractor accepts some
 * object with placeholder and returns required data from this object
 */
public interface ValueExtractor {

    /**
     * Get some data from given object by placeholder.
     * You should cast object to required type first
     * @param obj Any object
     * @param placeholder Placeholder key. Example: "location_x"
     * @return Resulting value in String format, or null
     */
    String extract(Object obj, String placeholder);

}
