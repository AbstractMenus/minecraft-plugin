package ru.abstractmenus.handlers.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.handler.PlaceholderHandler;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.placeholders.PAPIPlaceholders;

import java.util.List;

public class PlaceholderCustomHandler implements PlaceholderHandler {

    @Override
    public String replacePlaceholder(Player player, String placeholder) {
        return Colors.of(PlaceholderAPI.setPlaceholders(player, "%" + placeholder + "%"));
    }

    @Override
    public String replace(Player player, String str) {
        return Colors.of(PlaceholderAPI.setPlaceholders(player, str));
    }

    @Override
    public List<String> replace(Player player, List<String> list) {
        return Colors.ofList(PlaceholderAPI.setPlaceholders(player, list));
    }

    @Override
    public void registerAll() {
        new PAPIPlaceholders.VarHook().register();
        new PAPIPlaceholders.VarPlayerHook().register();
        new PAPIPlaceholders.VarTempHook().register();
        new PAPIPlaceholders.VarTempPlayerHook().register();
        new PAPIPlaceholders.HeadAnimHook().register();
        new PAPIPlaceholders.CatalogHook().register();
        new PAPIPlaceholders.ActivatorHook().register();
        new PAPIPlaceholders.PlacedItemHook().register();
        new PAPIPlaceholders.TakenItemHook().register();
        new PAPIPlaceholders.MovedItemHook().register();
    }
}
