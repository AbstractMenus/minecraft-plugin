package ru.abstractmenus.api;

/**
 * Strategy for resolving placeholder keys against an arbitrary context object.
 *
 * <p>AbstractMenus calls extractors to substitute placeholders like
 * {@code %name%}, {@code %location_x%}, or {@code %region_id%} when rendering
 * menu items &mdash; the context object is whatever the caller provided
 * (a Bukkit {@code Event} from an {@link Activator}, a catalog element from
 * {@link Catalog#extractor()}, a {@code Block}, {@code Entity}, {@code Region},
 * &hellip;).
 *
 * <p>Implementations typically start by narrowing the {@link Object} argument
 * with an {@code instanceof} check and returning {@code null} for keys the
 * extractor doesn't recognize, so the placeholder pipeline can fall through to
 * the next extractor in the chain.
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * ValueExtractor locationExtractor = (obj, key) -> {
 *     if (!(obj instanceof Location loc)) return null;
 *     return switch (key) {
 *         case "world" -> loc.getWorld().getName();
 *         case "x"     -> String.valueOf(loc.getBlockX());
 *         case "y"     -> String.valueOf(loc.getBlockY());
 *         case "z"     -> String.valueOf(loc.getBlockZ());
 *         default      -> null;
 *     };
 * };
 * }</pre>
 *
 * @see Activator#getValueExtractor()
 * @see Catalog#extractor()
 */
public interface ValueExtractor {

    /**
     * Resolve {@code placeholder} against {@code obj} and return the String
     * value that should be substituted in the rendered text.
     *
     * <p>Return {@code null} when the extractor doesn't recognize the key or
     * when {@code obj} is not of a type this extractor handles &mdash;
     * AbstractMenus will then consult the next extractor in the chain.
     *
     * @param obj         the context object; narrow with {@code instanceof}
     *                    before dereferencing
     * @param placeholder the placeholder key (no surrounding {@code %}); e.g.
     *                    {@code "location_x"} or {@code "name"}
     * @return the resolved value as a {@link String}, or {@code null} if this
     *         extractor cannot resolve the key against {@code obj}
     *
     * @implNote Called on the main server thread during menu rendering &mdash;
     *           keep the body cheap and side-effect free.
     */
    String extract(Object obj, String placeholder);

}
