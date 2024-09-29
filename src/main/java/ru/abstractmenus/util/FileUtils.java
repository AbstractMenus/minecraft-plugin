package ru.abstractmenus.util;

import ru.abstractmenus.AbstractMenus;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public final class FileUtils {

    private FileUtils(){}

    public static String getExtension(String name){
        String[] arr = name.split("\\.");
        return (arr.length > 0) ? arr[arr.length-1] : null;
    }

    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        } else {
            int lastUnixPos = filename.lastIndexOf(47);
            int lastWindowsPos = filename.lastIndexOf(92);
            return Math.max(lastUnixPos, lastWindowsPos);
        }
    }

    public static String getBaseName(String filename) {
        return removeExtension(getName(filename));
    }

    public static String removeExtension(String filename) {
        if (filename == null) {
            return null;
        } else {
            failIfNullBytePresent(filename);
            int index = indexOfExtension(filename);
            return index == -1 ? filename : filename.substring(0, index);
        }
    }

    public static String getName(String filename) {
        if (filename == null) {
            return null;
        } else {
            failIfNullBytePresent(filename);
            int index = indexOfLastSeparator(filename);
            return filename.substring(index + 1);
        }
    }

    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        } else {
            int extensionPos = filename.lastIndexOf(46);
            int lastSeparator = indexOfLastSeparator(filename);
            return lastSeparator > extensionPos ? -1 : extensionPos;
        }
    }

    public static String getResourceAsString(String path) {
        InputStream is = AbstractMenus.class.getResourceAsStream(path);

        if (is != null) {
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            return buf.lines().collect(Collectors.joining("\n"));
        }

        return null;
    }

    private static void failIfNullBytePresent(String path) {
        int len = path.length();

        for(int i = 0; i < len; ++i) {
            if (path.charAt(i) == 0) {
                throw new IllegalArgumentException("Null byte present in file/path name. There are no known legitimate use cases for such data, but several injection attacks may use it");
            }
        }
    }

}
