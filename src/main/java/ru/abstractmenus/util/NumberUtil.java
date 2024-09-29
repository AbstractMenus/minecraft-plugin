package ru.abstractmenus.util;

public final class NumberUtil {

    private NumberUtil() { }

    public static double round(double num, double digit) {
        return (double) Math.round(num * digit) / digit;
    }

    public static boolean canBeInt(double num) {
        return (int) num == num;
    }

    public static Number tryToInt(double value) {
        if (canBeInt(value)) {
            return (int) value;
        }
        return value;
    }

    public static String tryToInt(String value) {
        try {
            return String.valueOf(tryToInt(Double.parseDouble(value)));
        } catch (NumberFormatException e) {
            return value;
        }
    }


}
