package ru.abstractmenus.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestNumberUtil {

    @Test
    void roundToIntegerDigitsOne() {
        assertEquals(3.0, NumberUtil.round(3.14159, 1), 0.0);
    }

    @Test
    void roundToTwoDecimals() {
        assertEquals(3.14, NumberUtil.round(3.14159, 100), 0.0);
    }

    @Test
    void roundAwayFromZero() {
        assertEquals(3.15, NumberUtil.round(3.145, 100), 0.0);
    }

    @Test
    void roundNegative() {
        assertEquals(-3.14, NumberUtil.round(-3.14159, 100), 0.0);
    }

    @Test
    void canBeIntTrueForWhole() {
        assertTrue(NumberUtil.canBeInt(5.0));
        assertTrue(NumberUtil.canBeInt(-7.0));
        assertTrue(NumberUtil.canBeInt(0.0));
    }

    @Test
    void canBeIntFalseForFraction() {
        assertFalse(NumberUtil.canBeInt(5.5));
        assertFalse(NumberUtil.canBeInt(0.1));
    }

    @Test
    void tryToIntDoubleReturnsIntegerForWhole() {
        Number result = NumberUtil.tryToInt(7.0);
        assertEquals(Integer.class, result.getClass());
        assertEquals(7, result.intValue());
    }

    @Test
    void tryToIntDoubleReturnsDoubleForFraction() {
        Number result = NumberUtil.tryToInt(7.25);
        assertEquals(Double.class, result.getClass());
        assertEquals(7.25, result.doubleValue(), 0.0);
    }

    @Test
    void tryToIntStringReturnsIntegerString() {
        assertEquals("7", NumberUtil.tryToInt("7.0"));
        assertEquals("-3", NumberUtil.tryToInt("-3.0"));
    }

    @Test
    void tryToIntStringKeepsFractionalAsDouble() {
        assertEquals("7.25", NumberUtil.tryToInt("7.25"));
    }

    @Test
    void tryToIntStringReturnsOriginalOnInvalidInput() {
        assertEquals("abc", NumberUtil.tryToInt("abc"));
        assertEquals("", NumberUtil.tryToInt(""));
    }
}
