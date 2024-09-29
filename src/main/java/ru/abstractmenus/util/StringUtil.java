package ru.abstractmenus.util;

import java.util.concurrent.ThreadLocalRandom;

public final class StringUtil {

    private static final String ALPHABET = "0123456789_QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm";

    private StringUtil(){}

    public static boolean contains(String str, char ch){
        char[] arr = str.toCharArray();
        for (char c : arr) if (c == ch) return true;
        return false;
    }

    public static String generateRandom(int length) {
        return generateRandom(length, length + 1);
    }

    public static String generateRandom(int min, int max) {
        StringBuilder builder = new StringBuilder();
        int length = ThreadLocalRandom.current().nextInt(min, max);

        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(ALPHABET.length());
            builder.append(ALPHABET.charAt(index));
        }

        return builder.toString();
    }

    public static String replaceKeyPrefix(String key) {
        return key.replaceFirst("_+", "");
    }
}
