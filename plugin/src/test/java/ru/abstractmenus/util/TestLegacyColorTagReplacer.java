package ru.abstractmenus.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for {@link LegacyColorTagReplacer#replaceLegacyTags(String)}.
 *
 * Pins the single-pass {@code appendReplacement} rewrite that replaced the
 * O(K×N) re-create-Matcher loop. Inputs cover every legacy code, the fast
 * path, unknown codes, multi-code strings, and adversarial cases that
 * previously triggered quadratic re-scanning.
 */
class TestLegacyColorTagReplacer {

    private static final char S = LegacyColorTagReplacer.SECTION_CHAR;

    @Test
    void plainTextWithoutCodesReturnsSameReference() {
        // Fast-path: matcher.find() misses on first call → no StringBuilder alloc.
        String input = "Hello world";
        assertSame(input, LegacyColorTagReplacer.replaceLegacyTags(input));
    }

    @Test
    void emptyStringReturnsSameReference() {
        String input = "";
        assertSame(input, LegacyColorTagReplacer.replaceLegacyTags(input));
    }

    @Test
    void singleColourCodeReplaced() {
        assertEquals("<green>Hello",
                LegacyColorTagReplacer.replaceLegacyTags(S + "aHello"));
    }

    @Test
    void allColourCodesReplaced() {
        // 16 colour codes
        assertEquals("<black>", LegacyColorTagReplacer.replaceLegacyTags(S + "0"));
        assertEquals("<dark_blue>", LegacyColorTagReplacer.replaceLegacyTags(S + "1"));
        assertEquals("<dark_green>", LegacyColorTagReplacer.replaceLegacyTags(S + "2"));
        assertEquals("<dark_aqua>", LegacyColorTagReplacer.replaceLegacyTags(S + "3"));
        assertEquals("<dark_red>", LegacyColorTagReplacer.replaceLegacyTags(S + "4"));
        assertEquals("<dark_purple>", LegacyColorTagReplacer.replaceLegacyTags(S + "5"));
        assertEquals("<gold>", LegacyColorTagReplacer.replaceLegacyTags(S + "6"));
        assertEquals("<gray>", LegacyColorTagReplacer.replaceLegacyTags(S + "7"));
        assertEquals("<dark_gray>", LegacyColorTagReplacer.replaceLegacyTags(S + "8"));
        assertEquals("<blue>", LegacyColorTagReplacer.replaceLegacyTags(S + "9"));
        assertEquals("<green>", LegacyColorTagReplacer.replaceLegacyTags(S + "a"));
        assertEquals("<aqua>", LegacyColorTagReplacer.replaceLegacyTags(S + "b"));
        assertEquals("<red>", LegacyColorTagReplacer.replaceLegacyTags(S + "c"));
        assertEquals("<light_purple>", LegacyColorTagReplacer.replaceLegacyTags(S + "d"));
        assertEquals("<yellow>", LegacyColorTagReplacer.replaceLegacyTags(S + "e"));
        assertEquals("<white>", LegacyColorTagReplacer.replaceLegacyTags(S + "f"));
    }

    @Test
    void allFormatCodesReplaced() {
        assertEquals("<obf>", LegacyColorTagReplacer.replaceLegacyTags(S + "k"));
        assertEquals("<b>", LegacyColorTagReplacer.replaceLegacyTags(S + "l"));
        assertEquals("<st>", LegacyColorTagReplacer.replaceLegacyTags(S + "m"));
        assertEquals("<u>", LegacyColorTagReplacer.replaceLegacyTags(S + "n"));
        assertEquals("<i>", LegacyColorTagReplacer.replaceLegacyTags(S + "o"));
        assertEquals("<reset>", LegacyColorTagReplacer.replaceLegacyTags(S + "r"));
    }

    @Test
    void multipleCodesInOnePassWithText() {
        String result = LegacyColorTagReplacer.replaceLegacyTags(
                S + "aGreen " + S + "lBold " + S + "cRed");
        assertEquals("<green>Green <b>Bold <red>Red", result);
    }

    @Test
    void codeAtEndOfString() {
        assertEquals("Hello <reset>",
                LegacyColorTagReplacer.replaceLegacyTags("Hello " + S + "r"));
    }

    @Test
    void codeAtStartFollowedByText() {
        assertEquals("<green>Hi",
                LegacyColorTagReplacer.replaceLegacyTags(S + "aHi"));
    }

    @Test
    void unknownCodeXIsTreatedAsValidByPattern() {
        // Pattern matches §x but COLOR_TAGS has no "x" key — original §x is preserved.
        assertEquals(S + "x42",
                LegacyColorTagReplacer.replaceLegacyTags(S + "x42"));
    }

    @Test
    void unknownCodeOutsidePatternRangeKeptAsIs() {
        // §z is not even matched by the Pattern (range is 0-9, a-f, k-r, x).
        // Pattern.find() returns false → fast path returns input as-is.
        String input = S + "zHello";
        assertSame(input, LegacyColorTagReplacer.replaceLegacyTags(input));
    }

    @Test
    void mixedKnownAndUnknownCodes() {
        String result = LegacyColorTagReplacer.replaceLegacyTags(
                S + "aGreen " + S + "xUnknown " + S + "lBold");
        // §x is matched by pattern but unmapped → kept verbatim;
        // §a and §l replaced.
        assertEquals("<green>Green " + S + "xUnknown <b>Bold", result);
    }

    @Test
    void thirtyConsecutiveCodesProcessedInOnePassNotQuadratic() {
        // The old impl re-created Matcher 30 times and re-scanned from 0
        // each iteration. New impl uses single-pass appendReplacement.
        // Just assert correctness — perf is asserted by JMH benchmarks.
        StringBuilder input = new StringBuilder();
        StringBuilder expected = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            input.append(S).append('a').append('X');
            expected.append("<green>X");
        }
        assertEquals(expected.toString(),
                LegacyColorTagReplacer.replaceLegacyTags(input.toString()));
    }

    @Test
    void noAlterationOfMiniMessageTagsInString() {
        // Anything that's already a MiniMessage tag is left alone.
        String input = "<red>boom</red> normal text";
        assertSame(input, LegacyColorTagReplacer.replaceLegacyTags(input));
    }

    @Test
    void mixedMiniMessageAndLegacyTags() {
        String result = LegacyColorTagReplacer.replaceLegacyTags(
                "<red>boom</red> " + S + "aafter");
        assertEquals("<red>boom</red> <green>after", result);
    }

    @Test
    void replacementValuesNeedNoQuoting() {
        // Verifies that '<' and '>' in the replacement text don't trip
        // appendReplacement (only $ and \ are special there).
        String result = LegacyColorTagReplacer.replaceLegacyTags(S + "a");
        assertEquals("<green>", result);
        assertFalse(result.contains("$"));
    }
}
