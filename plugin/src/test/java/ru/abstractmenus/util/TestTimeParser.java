package ru.abstractmenus.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestTimeParser {

    private final TimeParser parser = new TimeParser();

    @Test
    void parsesSecondsOnly() {
        assertEquals(5_000L, parser.fromString("5s"));
    }

    @Test
    void parsesMinutesOnly() {
        assertEquals(60_000L, parser.fromString("1m"));
    }

    @Test
    void parsesHoursOnly() {
        assertEquals(3_600_000L, parser.fromString("1h"));
    }

    @Test
    void parsesDaysOnly() {
        assertEquals(86_400_000L, parser.fromString("1d"));
    }

    @Test
    void parsesMultiUnitExpression() {
        long expected = 86_400_000L + 3_600_000L + 60_000L + 1_000L;
        assertEquals(expected, parser.fromString("1d1h1m1s"));
    }

    @Test
    void parsesNonOrderedUnits() {
        assertEquals(60_000L + 1_000L, parser.fromString("1m1s"));
    }

    @Test
    void trailingBareNumberIsIgnored() {
        assertEquals(60_000L, parser.fromString("1m5"));
    }

    @Test
    void formatsZeroAsZeroSec() {
        assertEquals("0sec", parser.toString(0L));
    }

    @Test
    void formatsSecondsOnly() {
        assertEquals("5sec", parser.toString(5_000L));
    }

    @Test
    void formatsMinutesAndSeconds() {
        assertEquals("1min 30sec", parser.toString(90_000L));
    }

    @Test
    void formatsAllUnits() {
        long millis = 86_400_000L + 3_600_000L + 60_000L + 1_000L;
        assertEquals("1d 1h 1min 1sec", parser.toString(millis));
    }

    @Test
    void formatsSkipsZeroedUnits() {
        // 1 day + 5 seconds, hours/minutes are 0
        long millis = 86_400_000L + 5_000L;
        assertEquals("1d 5sec", parser.toString(millis));
    }

    @Test
    void customUnitSuffixesAreRespected() {
        TimeParser custom = new TimeParser("д", "ч", "мин", "с");
        assertEquals("1д 5с", custom.toString(86_400_000L + 5_000L));
    }

    @Test
    void roundTripFromStringBackToString() {
        long millis = parser.fromString("2d3h4m5s");
        assertEquals("2d 3h 4min 5sec", parser.toString(millis));
    }
}
