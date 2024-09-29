package ru.abstractmenus.data.catalogs;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Catalog;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.extractors.WorldExtractor;

import java.util.Collection;

public class WorldCatalog implements Catalog<World> {

    @Override
    public Collection<World> snapshot(Player player, Menu menu) {
        return Bukkit.getWorlds();
    }

    @Override
    public ValueExtractor extractor() {
        return WorldExtractor.INSTANCE;
    }

    public static class Serializer implements NodeSerializer<WorldCatalog> {

        @Override
        public WorldCatalog deserialize(Class<WorldCatalog> type, ConfigNode node) throws NodeSerializeException {
            return new WorldCatalog();
        }
    }
}
