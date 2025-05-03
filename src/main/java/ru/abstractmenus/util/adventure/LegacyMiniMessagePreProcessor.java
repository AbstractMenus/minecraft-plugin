package ru.abstractmenus.util.adventure;

import java.util.function.UnaryOperator;

class LegacyMiniMessagePreProcessor implements UnaryOperator<String> {

    @Override
    public String apply(String component) {
        return component.replace('§', '&');
    }
}