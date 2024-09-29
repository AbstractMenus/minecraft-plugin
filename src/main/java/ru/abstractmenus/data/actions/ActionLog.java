package ru.abstractmenus.data.actions;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

public class ActionLog implements Action {

    private final String message;

    private ActionLog(String message){
        this.message = message;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        Logger.info(Handlers.getPlaceholderHandler().replace(player, message));
    }

    public static class Serializer implements NodeSerializer<ActionLog> {

        @Override
        public ActionLog deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionLog(node.getString());
        }

    }
}
