package ru.abstractmenus.data.catalogs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Catalog;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.extractors.PlayerExtractor;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.Collection;

public class PlayerCatalog implements Catalog<Player> {

    @Override
    public Collection<Player> snapshot(Player player, Menu menu) {
        return (Collection<Player>) Bukkit.getOnlinePlayers();
    }

    @Override
    public ValueExtractor extractor() {
        return PlayerExtractor.INSTANCE;
    }

    public static class Serializer implements NodeSerializer<PlayerCatalog> {

        @Override
        public PlayerCatalog deserialize(Class<PlayerCatalog> type, ConfigNode node) throws NodeSerializeException {
            return new PlayerCatalog();
        }
    }
}
