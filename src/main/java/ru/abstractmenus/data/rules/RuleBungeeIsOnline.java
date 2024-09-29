package ru.abstractmenus.data.rules;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.services.BungeeManager;

public class RuleBungeeIsOnline implements Rule {

    private final String server;

    private RuleBungeeIsOnline(String server){
        this.server = server;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        return BungeeManager.instance().isOnline(Handlers.getPlaceholderHandler().replace(player, server));
    }

    public static class Serializer implements NodeSerializer<RuleBungeeIsOnline> {

        @Override
        public RuleBungeeIsOnline deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleBungeeIsOnline(node.getString());
        }

    }
}
