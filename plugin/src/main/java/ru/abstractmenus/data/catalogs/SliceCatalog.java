package ru.abstractmenus.data.catalogs;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Catalog;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SliceCatalog implements Catalog<String> {

    private static final SliceExtractor EXTRACTOR = new SliceExtractor();

    private final String value;
    private final String separator;

    private final Function<String, String> mapper;

    public SliceCatalog(String value, String separator, boolean trim) {
        this.value = value;
        this.separator = separator;

        if (trim) {
            mapper = String::trim;
        } else {
            mapper = val -> val;
        }
    }

    @Override
    public Collection<String> snapshot(Player player, Menu menu) {
        String replaced = Handlers.getPlaceholderHandler().replace(player, value);
        String[] values = replaced.split(separator);
        return Arrays.stream(values)
                .filter(val -> !val.isEmpty())
                .map(mapper)
                .collect(Collectors.toList());
    }

    @Override
    public ValueExtractor extractor() {
        return EXTRACTOR;
    }

    public static class SliceExtractor implements ValueExtractor {
        @Override
        public String extract(Object obj, String placeholder) {
            if (placeholder.equals("slice_element"))
                return obj.toString();
            return null;
        }
    }

    public static class Serializer implements NodeSerializer<SliceCatalog> {

        @Override
        public SliceCatalog deserialize(Class<SliceCatalog> type, ConfigNode node) throws NodeSerializeException {
            String value = node.node("value").getString();
            String separator = node.node("separator").getString();
            boolean trim = node.node("trim").getBoolean(true);
            return new SliceCatalog(value, separator, trim);
        }
    }
}
