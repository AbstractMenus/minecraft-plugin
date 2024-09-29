package ru.abstractmenus.data.rules;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.services.BungeeManager;
import ru.abstractmenus.datatype.TypeInt;

public class RuleBungeeOnline implements Rule {

    private final String server;
    private final TypeInt online;

    private RuleBungeeOnline(String server, TypeInt online){
        this.server = server;
        this.online = online;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        return BungeeManager.instance().getOnline(Handlers.getPlaceholderHandler().replace(player, server)) >= online.getInt(player, menu);
    }

    public static class Serializer implements NodeSerializer<RuleBungeeOnline> {

        @Override
        public RuleBungeeOnline deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            String server = node.node("server").getString();
            TypeInt online = node.node("online").getValue(TypeInt.class);
            return new RuleBungeeOnline(server, online);
        }
    }
}
