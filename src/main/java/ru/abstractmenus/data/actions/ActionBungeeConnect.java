package ru.abstractmenus.data.actions;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.services.BungeeManager;
import ru.abstractmenus.api.Action;

public class ActionBungeeConnect implements Action {

    private final String serverName;

    private ActionBungeeConnect(String serverName){
        this.serverName = serverName;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        String replaced = Handlers.getPlaceholderHandler().replace(player, serverName);
        BungeeManager.instance().sendPluginMessage(player, "Connect", replaced);
    }

    public static class Serializer implements NodeSerializer<ActionBungeeConnect> {

        @Override
        public ActionBungeeConnect deserialize(Class type, ConfigNode node) {
            return new ActionBungeeConnect(node.getString());
        }

    }
}
