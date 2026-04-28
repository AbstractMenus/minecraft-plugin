package ru.abstractmenus.api.text;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for converting user-facing color notation to Minecraft section-code strings.
 *
 * <p>{@code Colors} is the single entry point for all text colorization in AbstractMenus.
 * Every menu title, item display name, lore line, action message, and rule feedback string
 * passes through {@link #of(String)} before being handed to the Bukkit API. Two
 * transformations are applied, depending on server capabilities:
 *
 * <ul>
 *   <li><strong>Legacy {@code &}-codes</strong> &mdash; any {@code &} followed by a
 *       recognized Minecraft color/format character ({@code 0}&ndash;{@code 9},
 *       {@code a}&ndash;{@code f}, {@code k}&ndash;{@code r}) is translated to the
 *       corresponding {@code §} (section-sign) sequence via
 *       {@link ChatColor#translateAlternateColorCodes}. This works on all server
 *       versions.</li>
 *   <li><strong>Hex RGB codes</strong> &mdash; when the server ships BungeeCord's
 *       {@code net.md_5.bungee.api.ChatColor#of(String)} (1.16+), the pattern
 *       {@code <#RRGGBB>} (e.g. {@code <#FF0000>}) is additionally recognised and
 *       expanded to the BungeeCord hex-color sequence before legacy translation
 *       runs.</li>
 * </ul>
 *
 * <p>The active strategy ({@link SimpleReplacer} vs {@link RgbReplacer}) is chosen once
 * at startup by {@link #init(boolean)} and stored in {@link #replacer}. All subsequent
 * calls to {@code of*} methods are lock-free reads of that field.
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * // Input  : "&aHello <#FF0000>World"
 * // Output : "§aHello §x§F§F§0§0§0§0World"
 * String colored = Colors.of("&aHello <#FF0000>World");
 * }</pre>
 *
 * <h2>Performance</h2>
 *
 * <p>When hex support is active, {@link RgbReplacer} processes each hex token by
 * rebuilding the entire string &mdash; the cost is {@code O(n &times; k)} where {@code n}
 * is the string length and {@code k} is the number of hex tokens. {@code Colors.of} is
 * called on every user-facing string at display time (titles, lore&hellip;), so menus
 * with many hex tokens in long lore lists will allocate proportionally. Pre-colorize
 * strings at load time rather than on every inventory open to avoid repeated allocation.
 *
 * @see ChatColor#translateAlternateColorCodes(char, String)
 * @see net.md_5.bungee.api.ChatColor
 */
public class Colors {

    private static final char COLOR_PREFIX = '&';
    private static Replacer replacer;

    /**
     * Selects and initializes the color-replacement strategy.
     *
     * <p>Must be called exactly once during plugin startup, before any call to
     * {@link #of(String)}. AbstractMenus calls this internally; addon code must
     * <em>not</em> call it again.
     *
     * <p>The decision tree:
     * <ol>
     *   <li>If {@code replaceRgb} is {@code false} &mdash; always use the legacy
     *       {@code &}-only {@link SimpleReplacer}, regardless of server version.</li>
     *   <li>If {@code replaceRgb} is {@code true} and the server provides
     *       {@code net.md_5.bungee.api.ChatColor#of(String)} (detected via reflection)
     *       &mdash; activate {@link RgbReplacer} which handles both {@code <#RRGGBB>}
     *       and {@code &}-codes.</li>
     *   <li>Otherwise &mdash; fall back to {@link SimpleReplacer}.</li>
     * </ol>
     *
     * @param replaceRgb {@code true} to enable {@code <#RRGGBB>} hex expansion when the
     *                   server supports it; {@code false} to restrict to legacy codes only
     *
     * @implNote RGB capability is probed by loading
     *           {@code net.md_5.bungee.api.ChatColor} and reflectively looking up the
     *           {@code of(String)} method. Any {@link Throwable} is silently caught,
     *           treating the server as legacy-only.
     *
     * Set-once: subsequent calls are silently skipped so an addon cannot
     * flip the global RGB-handling mode. The skip (rather than throw)
     * keeps Bukkit {@code /reload} working; the trade-off is that a
     * config flip of {@code useMiniMessage} doesn't take effect until
     * a server restart.
     */
    public static void init(boolean replaceRgb) {
        if (replacer != null) return;
        if (isSupportRgb() && replaceRgb) {
            replacer = new RgbReplacer();
        } else {
            replacer = new SimpleReplacer();
        }
    }

    /**
     * Translates all color codes in {@code line} to Minecraft section-sign sequences.
     *
     * <p>The exact transformations applied depend on which strategy was selected by
     * {@link #init(boolean)}:
     * <ul>
     *   <li>{@link SimpleReplacer} &mdash; translates {@code &X} legacy codes only.</li>
     *   <li>{@link RgbReplacer} &mdash; first expands {@code <#RRGGBB>} hex tokens, then
     *       translates {@code &X} legacy codes.</li>
     * </ul>
     *
     * @param line the raw input string, possibly containing {@code &}-codes and/or
     *             {@code <#RRGGBB>} tokens; may be empty but must not be {@code null}
     * @return a new string with all recognized color codes replaced by their
     *         section-sign equivalents, ready for use in Bukkit display names, lore,
     *         titles, and action-bar messages
     */
    public static String of(String line) {
        return replacer.replace(line);
    }

    /**
     * Translates color codes in every element of {@code list} in-place.
     *
     * <p>Each element is replaced by the result of {@link #of(String)}. The list
     * itself is mutated and then returned &mdash; no defensive copy is made.
     *
     * @param list a mutable list of raw strings; must not be {@code null}
     * @return the same {@code list} instance with all elements colorized
     */
    public static List<String> ofList(List<String> list){
        for(int i = 0; i < list.size(); i++){
            list.set(i, of(list.get(i)));
        }

        return list;
    }

    /**
     * Translates color codes in every element of {@code array} in-place.
     *
     * <p>Each element is replaced by the result of {@link #of(String)}. The array
     * itself is mutated and then returned &mdash; no defensive copy is made.
     *
     * @param array a mutable array of raw strings; must not be {@code null}
     * @return the same {@code array} reference with all elements colorized
     */
    public static String[] ofArr(String[] array){
        for (int i = 0; i < array.length; i++){
            array[i] = of(array[i]);
        }
        return array;
    }

    /**
     * Returns {@code true} if the running server provides BungeeCord's
     * {@code net.md_5.bungee.api.ChatColor#of(String)} method, which is required
     * for hex-color expansion.
     *
     * @return {@code true} when hex RGB support is available; {@code false} on legacy
     *         servers or in environments where BungeeCord classes are absent
     *
     * @implNote Uses {@link Class#forName(String)} and
     *           {@link Class#getDeclaredMethod(String, Class[])} to probe at runtime.
     *           Any {@link Throwable} (including {@link NoClassDefFoundError}) is caught
     *           and treated as "not supported".
     */
    private static boolean isSupportRgb() {
        try {
            Class.forName("net.md_5.bungee.api.ChatColor")
                    .getDeclaredMethod("of", String.class);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Internal strategy for string color replacement.
     *
     * <p>Implementations are selected once by {@link Colors#init(boolean)} and must be
     * stateless &mdash; the single shared instance is called concurrently.
     */
    private interface Replacer {
        /**
         * Translates all recognized color codes in {@code input}.
         *
         * @param input raw string; must not be {@code null}
         * @return colorized string
         */
        String replace(String input);
    }

    /**
     * {@link Replacer} that handles only legacy {@code &X} color codes.
     *
     * <p>Used on servers that predate 1.16 or when hex support is explicitly disabled
     * via {@link Colors#init(boolean) Colors.init(false)}.
     */
    private static class SimpleReplacer implements Replacer {

        @Override
        public String replace(String input) {
            return ChatColor.translateAlternateColorCodes(COLOR_PREFIX, input);
        }
    }

    /**
     * {@link Replacer} that expands {@code <#RRGGBB>} hex tokens before translating
     * legacy {@code &X} codes.
     *
     * <p>Hex tokens are matched by {@link #PATTERN} and replaced with the BungeeCord
     * section-sign sequence produced by
     * {@code net.md_5.bungee.api.ChatColor#of(String)}. After all tokens are expanded,
     * {@link ChatColor#translateAlternateColorCodes} handles the remaining {@code &X}
     * codes.
     *
     * @implNote Each hex replacement rebuilds the full string and resets the
     *           {@link Matcher}, giving {@code O(n &times; k)} allocation where
     *           {@code n} is the string length and {@code k} is the number of hex
     *           tokens. Strings should be colorized once at load time rather than on
     *           every display to keep hot paths allocation-free.
     */
    private static class RgbReplacer implements Replacer {

        /**
         * Matches hex color tokens in the form {@code <#RRGGBB>}, case-insensitive.
         * Captures exactly six hexadecimal digits between {@code <#} and {@code >}.
         */
        private static final Pattern PATTERN = Pattern.compile("<#([A-Fa-f0-9]){6}>");

        @Override
        public String replace(String input) {
            Matcher matcher = PATTERN.matcher(input);

            while (matcher.find()) {
                String group = matcher.group();
                net.md_5.bungee.api.ChatColor hexColor = net.md_5.bungee.api.ChatColor.of(group
                        .substring(1, group.length() - 1));
                String before = input.substring(0, matcher.start());
                String after = input.substring(matcher.end());
                input = before + hexColor + after;
                matcher = PATTERN.matcher(input);
            }

            return ChatColor.translateAlternateColorCodes(COLOR_PREFIX, input);
        }
    }

}
