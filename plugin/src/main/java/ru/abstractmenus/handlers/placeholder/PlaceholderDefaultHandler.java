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

    private static final Pattern PATTERN = Pattern.compile("%(\\S+)%");

    private final Map<String, PlaceholderHook> hooks = new HashMap<>();

    @Override
    public String replacePlaceholder(Player player, String placeholder) {
        return replace(player, "%" + placeholder + "%");
    }

    @Override
    public String replace(Player player, String str) {
        if (player == null || str.indexOf('%') == -1) return str;

        Matcher matcher = PATTERN.matcher(str);
        StringBuilder sb = null;

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            int sep = placeholder.indexOf('_');

            if (sep != -1) {
                String hookName = placeholder.substring(0, sep);
                PlaceholderHook hook = hooks.get(hookName);

                if (hook != null) {
                    String replaced = hook.replace(placeholder.substring(sep + 1), player);

                    if (replaced != null) {
                        if (sb == null) sb = new StringBuilder(str.length() + 32);
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(replaced));
                        continue;
                    }
                }
            }

            if (sb != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
            }
        }

        if (sb == null) return str;
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    public List<String> replace(Player player, List<String> list) {
        List<String> result = new ArrayList<>();

        for (String line : list) {
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
