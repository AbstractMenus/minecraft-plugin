package ru.abstractmenus.extractors;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.ValueExtractor;

public class PlayerExtractor implements ValueExtractor {

    public static final PlayerExtractor INSTANCE = new PlayerExtractor();

    @Override
    public String extract(Object obj, String placeholder) {
        return (obj instanceof Player && ((Player)obj).isOnline())
                ? Handlers.getPlaceholderHandler().replacePlaceholder((Player) obj, placeholder)
                : "";
    }
}
