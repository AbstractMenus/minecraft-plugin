package ru.abstractmenus.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.MainConfig;

import java.util.ArrayList;
import java.util.List;

public final class MiniMessageUtil {

    private static Replacer replacer;

    private MiniMessageUtil() {
    }

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
        private final LegacyComponentSerializer serializer = LegacyComponentSerializer.builder()
                .character(LegacyComponentSerializer.SECTION_CHAR)
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();

        @Override
        public void sendParsed(List<String> input, Player player) {
            if (input == null || input.isEmpty()) return;

            for (String line : input) {
                if (line != null) {
                    send(parse(LegacyColorTagReplacer.replaceLegacyTags(line)), player);
                }
            }
        }

        @Override
        public String parseToLegacy(String input) {
            if (input == null) return null;
            if (input.isEmpty()) return input;

            return serializer.serialize(parse(LegacyColorTagReplacer.replaceLegacyTags(input)));
        }

        @Override
        public List<String> parseToLegacy(List<String> input) {
            if (input == null) return null;
            if (input.isEmpty()) return input;

            List<String> out = new ArrayList<>(input.size());
            for (String line : input) {
                out.add(parseToLegacy(line));
            }
            return out;
        }

        private void send(Component component, Player player) {
            player.sendMessage(component);
        }

        private Component parse(String input) {
            return miniMessage.deserialize(input);
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
