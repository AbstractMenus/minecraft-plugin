package ru.abstractmenus.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestStringUtil {

    @Test
    void containsReturnsTrueForPresentChar() {
        assertTrue(StringUtil.contains("abcd", 'c'));
    }

    @Test
    void containsReturnsFalseForMissingChar() {
        assertFalse(StringUtil.contains("abcd", 'z'));
    }

    @Test
    void containsReturnsFalseForEmptyString() {
        assertFalse(StringUtil.contains("", 'a'));
    }

    @Test
    void containsMatchesFirstAndLastChar() {
        assertTrue(StringUtil.contains("abc", 'a'));
        assertTrue(StringUtil.contains("abc", 'c'));
    }

    @Test
    void generateRandomFixedLengthProducesExactLength() {
        String result = StringUtil.generateRandom(10);
        assertEquals(10, result.length());
    }

    @Test
    void generateRandomFixedLengthUsesAllowedAlphabet() {
        String alphabet = "0123456789_QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm";
        String result = StringUtil.generateRandom(100);
        for (char c : result.toCharArray()) {
            assertTrue(alphabet.indexOf(c) >= 0, "Unexpected char: " + c);
        }
    }

    @Test
    void generateRandomRangeStaysInBounds() {
        for (int i = 0; i < 50; i++) {
            String result = StringUtil.generateRandom(5, 10);
            assertTrue(result.length() >= 5 && result.length() < 10,
                    "Length out of range: " + result.length());
        }
    }

    @Test
    void replaceKeyPrefixStripsLeadingUnderscores() {
        assertEquals("name", StringUtil.replaceKeyPrefix("___name"));
    }

    @Test
    void replaceKeyPrefixKeepsUnderscoresInMiddle() {
        assertEquals("foo_bar", StringUtil.replaceKeyPrefix("_foo_bar"));
    }

    @Test
    void replaceKeyPrefixReturnsSameWhenNoLeadingUnderscore() {
        assertEquals("name", StringUtil.replaceKeyPrefix("name"));
    }

    @Test
    void replaceKeyPrefixReturnsEmptyForOnlyUnderscores() {
        assertEquals("", StringUtil.replaceKeyPrefix("___"));
    }
}
