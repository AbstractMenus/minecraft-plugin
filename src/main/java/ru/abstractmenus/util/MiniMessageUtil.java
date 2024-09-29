package ru.abstractmenus.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.MainConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MiniMessageUtil {

    private static Replacer replacer;

    private MiniMessageUtil() {}

    public static void init(MainConfig config) {
        if (config.isUseMiniMessage()) {
            replacer = new ActiveReplacer();
        } else {
            replacer = new InactiveReplacer();
        }
    }

    public static void init(boolean useMiniMessage) {
        if (useMiniMessage) {
            replacer = new ActiveReplacer();
        } else {
            replacer = new InactiveReplacer();
        }
    }

    public static void sendParsed(List<String> input, Player player) {
        replacer.sendParsed(input, player);
    }

    public static String parseToLegacy(String input) {
        return replacer.parseToLegacy(input);
    }

    public static List<String> parseToLegacy(List<String> input) {
        return replacer.parseToLegacy(input);
    }

    private interface Replacer {

        void sendParsed(List<String> input, Player player);

        String parseToLegacy(String input);

        List<String> parseToLegacy(List<String> input);

    }

    private static class ActiveReplacer implements Replacer {

        private final MiniMessage miniMessage = MiniMessage.miniMessage();
        private final char legacyChar = LegacyComponentSerializer.SECTION_CHAR;
        private final LegacyComponentSerializer serializer = LegacyComponentSerializer.builder()
                .character(LegacyComponentSerializer.SECTION_CHAR)
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();

        private final Pattern colorPattern = Pattern.compile(legacyChar + "([0-9a-fk-rx])");
        private final Map<String, String> colorTags = new HashMap<>();

        private ActiveReplacer() {
            colorTags.put("0", "<black>");
            colorTags.put("1", "<dark_blue>");
            colorTags.put("2", "<dark_green>");
            colorTags.put("3", "<dark_aqua>");
            colorTags.put("4", "<dark_red>");
            colorTags.put("5", "<dark_purple>");
            colorTags.put("6", "<gold>");
            colorTags.put("7", "<gray>");
            colorTags.put("8", "<dark_gray>");
            colorTags.put("9", "<blue>");
            colorTags.put("a", "<green>");
            colorTags.put("b", "<aqua>");
            colorTags.put("c", "<red>");
            colorTags.put("d", "<light_purple>");
            colorTags.put("e", "<yellow>");
            colorTags.put("f", "<white>");
            colorTags.put("k", "<obf>");
            colorTags.put("l", "<b>");
            colorTags.put("m", "<st>");
            colorTags.put("n", "<u>");
            colorTags.put("o", "<i>");
            colorTags.put("r", "<reset>");
        }

        @Override
        public void sendParsed(List<String> input, Player player) {
            if (input == null || input.isEmpty()) return;

            for (String line : input) {
                if (line != null) {
                    send(parse(replaceLegacyTags(line)), player);
                }
            }
        }

        @Override
        public String parseToLegacy(String input) {
            if (input == null) return null;
            if (input.isEmpty()) return input;

            return serializer.serialize(parse(replaceLegacyTags(input)));
        }

        @Override
        public List<String> parseToLegacy(List<String> input) {
            if (input == null) return null;
            if (input.isEmpty()) return input;

            return input.stream()
                    .map(this::parseToLegacy)
                    .collect(Collectors.toList());
        }

        private void send(Component component, Player player) {
            player.sendMessage(component);
        }

        private Component parse(String input) {
            return miniMessage.deserialize(input);
        }

        private String replaceLegacyTags(String input) {
            Matcher matcher = colorPattern.matcher(input);
            String replaced = input;

            while (matcher.find()) {
                String code = matcher.group(1);
                String replacement = colorTags.get(code);

                if (replacement != null) {
                    String before = replaced.substring(0, matcher.start());
                    String after = replaced.substring(matcher.end());
                    StringBuilder result = new StringBuilder(before);

                    replaced = result.append(replacement)
                            .append(after)
                            .toString();
                }
                matcher = colorPattern.matcher(replaced);
            }

            return replaced;
        }
    }

    private static class InactiveReplacer implements Replacer {

        @Override
        public void sendParsed(List<String> input, Player player) {
            input.forEach(player::sendMessage);
        }

        @Override
        public String parseToLegacy(String input) {
            return input;
        }

        @Override
        public List<String> parseToLegacy(List<String> input) {
            return input;
        }
    }

}
