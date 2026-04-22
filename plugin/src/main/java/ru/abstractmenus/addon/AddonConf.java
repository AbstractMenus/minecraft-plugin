package ru.abstractmenus.addon;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.source.ConfigSources;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Immutable metadata parsed from an addon's {@code addon.conf} file. Use
 * {@link #parse(String)} to construct from HOCON text.
 *
 * <p>Three of the nine fields are required ({@code name}, {@code version},
 * {@code main}); the rest default to empty or absent.
 */
public record AddonConf(
        String name,
        String version,
        String main,
        List<String> authors,
        String description,
        String targetApiVersion,
        List<String> addonDependencies,
        List<String> pluginDependencies,
        List<String> pluginSoftDependencies
) {

    /**
     * Parse HOCON text into an AddonConf.
     *
     * @param hocon raw text content of an {@code addon.conf} file
     * @return parsed conf
     * @throws AddonConfParseException if required fields are missing, fields
     *         have the wrong type, or the HOCON itself is malformed
     */
    public static AddonConf parse(String hocon) {
        ConfigNode root;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(
                    hocon.getBytes(StandardCharsets.UTF_8));
            root = ConfigurationLoader.builder()
                    .source(ConfigSources.inputStream("addon.conf", in))
                    .build()
                    .load();
        } catch (Exception e) {
            throw new AddonConfParseException("Failed to parse addon.conf: " + e.getMessage(), e);
        }

        String name        = requireString(root, "name");
        String version     = requireString(root, "version");
        String main        = requireString(root, "main");

        List<String> authors               = optionalStringList(root, "authors");
        String description                 = root.node("description").getString("");
        String targetApiVersion            = optionalString(root, "targetApiVersion");
        List<String> addonDependencies     = optionalStringList(root, "addonDependencies");
        List<String> pluginDependencies    = optionalStringList(root, "pluginDependencies");
        List<String> pluginSoftDependencies = optionalStringList(root, "pluginSoftDependencies");

        return new AddonConf(
                name, version, main,
                authors, description, targetApiVersion,
                addonDependencies, pluginDependencies, pluginSoftDependencies
        );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the string value of a required field.
     *
     * @throws AddonConfParseException if the field is absent, null, or blank
     */
    private static String requireString(ConfigNode root, String field) {
        ConfigNode node = root.node(field);
        if (node.isNull()) {
            throw new AddonConfParseException(
                    "Required field '" + field + "' is missing or blank");
        }
        String value = node.getString(null);
        if (value == null || value.isBlank()) {
            throw new AddonConfParseException(
                    "Required field '" + field + "' is missing or blank");
        }
        return value;
    }

    /**
     * Returns an optional string value, or {@code null} if absent.
     */
    private static String optionalString(ConfigNode root, String field) {
        ConfigNode node = root.node(field);
        if (node.isNull()) {
            return null;
        }
        return node.getString(null);
    }

    /**
     * Returns an optional list of strings. Accepts either a HOCON list or a
     * single string (auto-wrapped into a one-element list). Returns
     * {@link List#of()} if the field is absent.
     */
    private static List<String> optionalStringList(ConfigNode root, String field) {
        ConfigNode node = root.node(field);
        if (node.isNull()) {
            return List.of();
        }
        // Single-string scalar: wrap in a one-element list.
        if (node.isPrimitive()) {
            String value = node.getString(null);
            return value != null ? List.of(value) : List.of();
        }
        try {
            return List.copyOf(node.getList(String.class));
        } catch (Exception e) {
            throw new AddonConfParseException(
                    "Field '" + field + "' must be a string or list of strings: " + e.getMessage(), e);
        }
    }
}
