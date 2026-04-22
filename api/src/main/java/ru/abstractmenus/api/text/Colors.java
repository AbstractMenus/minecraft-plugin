package ru.abstractmenus.api.text;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Util to easy color codes replacing
 */
public class Colors {

    private static final char COLOR_PREFIX = '&';
    private static Replacer replacer;

    /**
     * Initialize util. Do not call this method manually
     * @param replaceRgb Replace RGB tags
     */
    public static void init(boolean replaceRgb) {
        if (isSupportRgb() && replaceRgb) {
            replacer = new RgbReplacer();
        } else {
            replacer = new SimpleReplacer();
        }
    }

    /**
     * Replace all color codes
     * @param line Required single string
     * @return String with replaced colors
     */
    public static String of(String line) {
        return replacer.replace(line);
    }

    /**
     * Replace all color codes
     * @param list Required strings list
     * @return Strings list with replaced colors
     */
    public static List<String> ofList(List<String> list){
        for(int i = 0; i < list.size(); i++){
            list.set(i, of(list.get(i)));
        }

        return list;
    }

    /**
     * Replace all color codes
     * @param array Required strings array
     * @return Strings array with replaced colors
     */
    public static String[] ofArr(String[] array){
        for (int i = 0; i < array.length; i++){
            array[i] = of(array[i]);
        }
        return array;
    }

    private static boolean isSupportRgb() {
        try {
            Class.forName("net.md_5.bungee.api.ChatColor")
                    .getDeclaredMethod("of", String.class);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private interface Replacer {
        String replace(String input);
    }

    private static class SimpleReplacer implements Replacer {

        @Override
        public String replace(String input) {
            return ChatColor.translateAlternateColorCodes(COLOR_PREFIX, input);
        }
    }

    private static class RgbReplacer implements Replacer {

        private static final Pattern PATTERN = Pattern.compile("<#([A-Fa-f0-9]){6}>");

        @Override
        public String replace(String input) {
            Matcher matcher = PATTERN.matcher(input);

            while (matcher.find()) {
                String group = matcher.group();
                net.md_5.bungee.api.ChatColor hexColor = net.md_5.bungee.api.ChatColor.of(group
                        .substring(1, group.length() - 1));
                String before = input.substring(0, matcher.start());
                String after = input.substring(matcher.end());
                input = before + hexColor + after;
                matcher = PATTERN.matcher(input);
            }

            return ChatColor.translateAlternateColorCodes(COLOR_PREFIX, input);
        }
    }

}
