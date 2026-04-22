package ru.abstractmenus.datatype;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestHasPlaceholder {

    @Test
    public void testPercentPlaceholder() {
        assertTrue(DataType.hasPlaceholder("%player_name%"));
    }

    @Test
    public void testSinglePercent() {
        assertTrue(DataType.hasPlaceholder("hello%world"));
    }

    @Test
    public void testDollarBracePlaceholder() {
        assertTrue(DataType.hasPlaceholder("${variable}"));
    }

    @Test
    public void testPlainString() {
        assertFalse(DataType.hasPlaceholder("STONE"));
    }

    @Test
    public void testEmptyString() {
        assertFalse(DataType.hasPlaceholder(""));
    }

    @Test
    public void testDollarWithoutBrace() {
        assertFalse(DataType.hasPlaceholder("$variable"));
    }

    @Test
    public void testBraceWithoutDollar() {
        assertFalse(DataType.hasPlaceholder("{variable}"));
    }
}
