package ru.abstractmenus.util.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class LegacyMiniMessagePostProcessor implements UnaryOperator<Component> {

    private final TextReplacementConfig config = TextReplacementConfig.builder()
            .match(Pattern.compile(".*"))
            .replacement((match, unused) -> LegacyComponentSerializer.legacyAmpersand().deserialize(match.group()))
            .build();

    @Override
    public Component apply(Component component) {
        return component.replaceText(config);
    }
}