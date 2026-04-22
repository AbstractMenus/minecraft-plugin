package ru.abstractmenus.data.catalogs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Catalog;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class IteratorCatalog implements Catalog<Integer> {

    private static final IteratorExtractor EXTRACTOR = new IteratorExtractor();

    private final String from;
    private final String to;
    private final boolean desc;

    @Override
    public Collection<Integer> snapshot(Player player, Menu menu) {
        List<Integer> list = new LinkedList<>();

        int fromInt = parseIntValue(player, from);
        int toInt = parseIntValue(player, to);


        if (desc) {
            for (int i = toInt; i > fromInt; i--) list.add(i);
        } else {
            for (int i = fromInt; i < toInt + 1; i++) list.add(i);
        }
        return list;
    }

    private int parseIntValue(Player player, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            String replaced = PlaceholderAPI.setPlaceholders(player, value);
            try {
                return Integer.parseInt(replaced);
            } catch (NumberFormatException e1) {
                log.error("Could not parse Integer value for placeholder '{}' for player '{}'. Set 0 as default", value, player.getName());
                return 0;
            }
        }
    }

    @Override
    public ValueExtractor extractor() {
        return EXTRACTOR;
    }

    public static class IteratorExtractor implements ValueExtractor {
        @Override
        public String extract(Object obj, String placeholder) {
            if (placeholder.equals("index"))
                return obj.toString();
            return null;
        }
    }

    public static class Serializer implements NodeSerializer<IteratorCatalog> {

        @Override
        public IteratorCatalog deserialize(Class<IteratorCatalog> type, ConfigNode node) throws NodeSerializeException {
            String from = node.node("start").getString();
            String to = node.node("end").getString();
            try {
                Integer.parseInt(from);
            } catch (NumberFormatException e) {
                if (!PlaceholderAPI.containsPlaceholders(from)) {
                    throw new NodeSerializeException("Invalid start value: '" + from + "' (not a number or placeholder)");
                }
            }
            try {
                Integer.parseInt(to);
            } catch (NumberFormatException e) {
                if (!PlaceholderAPI.containsPlaceholders(to)) {
                    throw new NodeSerializeException("Invalid end value: '" + to + "' (not a number or placeholder)");
                }
            }

            boolean desc = node.node("desc").getBoolean(false);
            return new IteratorCatalog(from, to, desc);
        }
    }
}
