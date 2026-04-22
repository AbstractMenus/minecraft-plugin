package ru.abstractmenus.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestFileUtils {

    @Test
    void getExtensionReturnsExtension() {
        assertEquals("conf", FileUtils.getExtension("menu.conf"));
    }

    @Test
    void getExtensionOfNameWithoutDotReturnsNull() {
        assertNull(FileUtils.getExtension("menu"));
    }

    @Test
    void getExtensionReturnsLastPart() {
        assertEquals("gz", FileUtils.getExtension("backup.tar.gz"));
    }

    @Test
    void getExtensionReturnsEmptyForTrailingDot() {
        assertEquals("", FileUtils.getExtension("trailing."));
    }

    @Test
    void indexOfLastSeparatorFindsUnixSlash() {
        assertEquals(3, FileUtils.indexOfLastSeparator("a/b/c"));
    }

    @Test
    void indexOfLastSeparatorFindsWindowsBackslash() {
        assertEquals(3, FileUtils.indexOfLastSeparator("a\\b\\c"));
    }

    @Test
    void indexOfLastSeparatorReturnsMinusOneWhenAbsent() {
        assertEquals(-1, FileUtils.indexOfLastSeparator("filename"));
    }

    @Test
    void indexOfLastSeparatorReturnsMinusOneForNull() {
        assertEquals(-1, FileUtils.indexOfLastSeparator(null));
    }

    @Test
    void indexOfExtensionReturnsDotPosition() {
        assertEquals(4, FileUtils.indexOfExtension("menu.conf"));
    }

    @Test
    void indexOfExtensionIgnoresDotsInDirectoryPart() {
        assertEquals(-1, FileUtils.indexOfExtension("a.b/c"));
    }

    @Test
    void indexOfExtensionReturnsMinusOneForNull() {
        assertEquals(-1, FileUtils.indexOfExtension(null));
    }

    @Test
    void getNameReturnsFilenameAfterUnixPath() {
        assertEquals("menu.conf", FileUtils.getName("/a/b/menu.conf"));
    }

    @Test
    void getNameReturnsFilenameAfterWindowsPath() {
        assertEquals("menu.conf", FileUtils.getName("C:\\a\\b\\menu.conf"));
    }

    @Test
    void getNameReturnsOriginalWhenNoSeparator() {
        assertEquals("menu.conf", FileUtils.getName("menu.conf"));
    }

    @Test
    void getNameReturnsNullForNull() {
        assertNull(FileUtils.getName(null));
    }

    @Test
    void getBaseNameStripsExtensionAndPath() {
        assertEquals("menu", FileUtils.getBaseName("/a/b/menu.conf"));
    }

    @Test
    void getBaseNameOnDirectoryPathWithDotKeepsFull() {
        // a.b directory with no filename extension after last separator
        assertEquals("c", FileUtils.getBaseName("a.b/c"));
    }

    @Test
    void getBaseNameHandlesMultipleDots() {
        assertEquals("backup.tar", FileUtils.getBaseName("backup.tar.gz"));
    }

    @Test
    void getBaseNameOfNameWithoutExtension() {
        assertEquals("README", FileUtils.getBaseName("README"));
    }

    @Test
    void getBaseNameReturnsNullForNull() {
        assertNull(FileUtils.getBaseName(null));
    }

    @Test
    void removeExtensionReturnsNullForNull() {
        assertNull(FileUtils.removeExtension(null));
    }

    @Test
    void removeExtensionRejectsNullByte() {
        assertThrows(IllegalArgumentException.class,
                () -> FileUtils.removeExtension("foo\u0000.txt"));
    }

    @Test
    void getNameRejectsNullByte() {
        assertThrows(IllegalArgumentException.class,
                () -> FileUtils.getName("foo\u0000bar"));
    }
}
