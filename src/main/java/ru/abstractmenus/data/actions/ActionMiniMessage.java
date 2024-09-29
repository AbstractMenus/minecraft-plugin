package ru.abstractmenus.data.actions;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.util.MiniMessageUtil;

import java.util.Collections;

public class ActionMiniMessage implements Action {

    private final String message;

    private ActionMiniMessage(String message) {
        this.message = message;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        String replaced = Handlers.getPlaceholderHandler().replace(player, message);
        MiniMessageUtil.sendParsed(Collections.singletonList(replaced), player);
    }

    public static class Serializer implements NodeSerializer<ActionMiniMessage> {

        @Override
        public ActionMiniMessage deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionMiniMessage(node.getString());
        }

    }
}
