package ru.abstractmenus.extractors;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.ValueExtractor;

public class PlayerExtractor implements ValueExtractor {

    public static final PlayerExtractor INSTANCE = new PlayerExtractor();

    @Override
    public String extract(Object obj, String placeholder) {
        return (obj instanceof Player player && player.isOnline())
                ? AbstractMenusApi.get().providers().placeholders().resolve().replacePlaceholder(player, placeholder)
                : StringUtils.EMPTY;
    }
}
