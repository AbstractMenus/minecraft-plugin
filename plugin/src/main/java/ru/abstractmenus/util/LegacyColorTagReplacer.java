package ru.abstractmenus.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts legacy color/format codes (§a, §l, §r ...) into the corresponding
 * MiniMessage tags (&lt;green&gt;, &lt;b&gt;, &lt;reset&gt; ...) so the result can be fed into
 * {@code MiniMessage.deserialize}.
 *
 * <p>Extracted from {@link MiniMessageUtil} so the legacy → MiniMessage conversion
 * can be unit-tested without triggering Adventure's
 * {@code LegacyComponentSerializer} static init (which conflicts on the test
 * classpath when multiple Adventure impl jars are present).
 *
 * <p>Performance-critical: the public {@link #replaceLegacyTags(String)} runs
 * once per name and once per lore line on every menu refresh — i.e. thousands
 * of times per second on a busy server. The implementation is deliberately
 * single-pass with one {@code Matcher} and one {@code StringBuilder}; the
 * fast-path returns the input string unchanged when no legacy code is present.
 */
final class LegacyColorTagReplacer {

    static final char SECTION_CHAR = '\u00a7';

    private static final Pattern COLOR_PATTERN = Pattern.compile(SECTION_CHAR + "([0-9a-fk-rx])");
    private static final Map<String, String> COLOR_TAGS = new HashMap<>();

    static {
        COLOR_TAGS.put("0", "<black>");
        COLOR_TAGS.put("1", "<dark_blue>");
        COLOR_TAGS.put("2", "<dark_green>");
        COLOR_TAGS.put("3", "<dark_aqua>");
        COLOR_TAGS.put("4", "<dark_red>");
        COLOR_TAGS.put("5", "<dark_purple>");
        COLOR_TAGS.put("6", "<gold>");
        COLOR_TAGS.put("7", "<gray>");
        COLOR_TAGS.put("8", "<dark_gray>");
        COLOR_TAGS.put("9", "<blue>");
        COLOR_TAGS.put("a", "<green>");
        COLOR_TAGS.put("b", "<aqua>");
        COLOR_TAGS.put("c", "<red>");
        COLOR_TAGS.put("d", "<light_purple>");
        COLOR_TAGS.put("e", "<yellow>");
        COLOR_TAGS.put("f", "<white>");
        COLOR_TAGS.put("k", "<obf>");
        COLOR_TAGS.put("l", "<b>");
        COLOR_TAGS.put("m", "<st>");
        COLOR_TAGS.put("n", "<u>");
        COLOR_TAGS.put("o", "<i>");
        COLOR_TAGS.put("r", "<reset>");
    }

    private LegacyColorTagReplacer() {}

    /**
     * Returns {@code input} with every recognised legacy color/format code
     * replaced by its MiniMessage tag. Unknown codes (e.g. {@code §z}) and
     * the surrounding text are preserved verbatim. Returns the original
     * reference when the input contains no legacy code (zero allocations).
     */
    static String replaceLegacyTags(String input) {
        Matcher matcher = COLOR_PATTERN.matcher(input);
        if (!matcher.find()) return input;

        StringBuilder sb = new StringBuilder(input.length() + 32);
        do {
            String replacement = COLOR_TAGS.get(matcher.group(1));
            // Replacement values contain '<' and '>' but neither '$' nor '\\',
            // so they are safe as appendReplacement literals without quoting.
            matcher.appendReplacement(sb, replacement != null ? replacement : matcher.group());
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }
}
