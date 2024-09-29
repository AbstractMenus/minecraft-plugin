package ru.abstractmenus.handlers.placeholder;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.handler.PlaceholderHandler;
import ru.abstractmenus.placeholders.hooks.*;
import ru.abstractmenus.placeholders.PlaceholderHook;
import ru.abstractmenus.placeholders.hooks.dnd.ChangedItemPlaceholders;
import ru.abstractmenus.placeholders.hooks.dnd.PlacedItemPlaceholders;
import ru.abstractmenus.placeholders.hooks.dnd.TakenItemPlaceholders;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderDefaultHandler implements PlaceholderHandler {

    private final Map<String, PlaceholderHook> hooks = new HashMap<>();
    private final Pattern pattern = Pattern.compile("%(\\S+)%");

    @Override
    public String replacePlaceholder(Player player, String placeholder) {
        return replace(player, "%" + placeholder + "%");
    }

    @Override
    public String replace(Player player, String str) {
        if (player == null) return str;

        String result = str;
        Matcher matcher = pattern.matcher(str);

        while(matcher.find()) {
            String placeholder = matcher.group(1);
            String[] arr = placeholder.split("_", 2);

            if (arr.length == 2) {
                PlaceholderHook hook = hooks.get(arr[0]);

                if (hook != null) {
                    String replaced = hook.replace(arr[1], player);
                    result = replaced != null ? result.replace("%"+placeholder+"%", replaced) : result;
                }
            }
        }

        return result;
    }

    @Override
    public List<String> replace(Player player, List<String> list) {
        List<String> result = new ArrayList<>();

        for (String line : list){
            result.add(replace(player, line));
        }

        return result;
    }

    @Override
    public void registerAll() {
        hooks.put("bungee", new BungeePlaceholders());
        hooks.put("hanim", new HeadAnimPlaceholders());
        hooks.put("player", new PlayerPlaceholders());
        hooks.put("server", new ServerPlaceholders());
        hooks.put("var", new VarPlaceholders.VarHook());
        hooks.put("varp", new VarPlaceholders.VarPlayerHook());
        hooks.put("vart", new VarPlaceholders.VarTempHook());
        hooks.put("varpt", new VarPlaceholders.VarTempPlayerHook());
        hooks.put("ctg", new CatalogPlaceholders());
        hooks.put("activator", new ActivatorPlaceholders());
        hooks.put("placed", new PlacedItemPlaceholders());
        hooks.put("taken", new TakenItemPlaceholders());
        hooks.put("changed", new ChangedItemPlaceholders());
    }
}
