package ru.abstractmenus.data.catalogs;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Catalog;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.services.BungeeManager;

import java.util.Collection;

public class ServerCatalog implements Catalog<String> {

    private static final ServerExtractor EXTRACTOR = new ServerExtractor();

    @Override
    public Collection<String> snapshot(Player player, Menu menu) {
        return BungeeManager.instance().getServers();
    }

    @Override
    public ValueExtractor extractor() {
        return EXTRACTOR;
    }

    private static final class ServerExtractor implements ValueExtractor {

        @Override
        public String extract(Object obj, String placeholder) {
            switch (placeholder) {
                default: return null;
                case "server_name": return obj.toString();
                case "server_online": return String.valueOf(BungeeManager.instance().getOnline(obj.toString()));
            }
        }

    }

    public static class Serializer implements NodeSerializer<ServerCatalog> {

        @Override
        public ServerCatalog deserialize(Class<ServerCatalog> type, ConfigNode node) throws NodeSerializeException {
            return new ServerCatalog();
        }
    }
}
