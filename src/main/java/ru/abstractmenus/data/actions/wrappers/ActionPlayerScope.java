package ru.abstractmenus.data.actions.wrappers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.data.Actions;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

public class ActionPlayerScope implements Action {

    private final String playerName;
    private final Actions actions;

    public ActionPlayerScope(String playerName, Actions actions) {
        this.playerName = playerName;
        this.actions = actions;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        String replacedName = Handlers.getPlaceholderHandler().replace(player, playerName);
        Player target = Bukkit.getPlayerExact(replacedName);

        if (target != null && target.isOnline()) {
            actions.activate(target, menu, clickedItem);
        }
    }

    public static class Serializer implements NodeSerializer<ActionPlayerScope> {

        @Override
        public ActionPlayerScope deserialize(Class<ActionPlayerScope> type, ConfigNode node) throws NodeSerializeException {
            String playerName = node.node("name").getString();
            Actions actions = node.node("actions").getValue(Actions.class);
            return new ActionPlayerScope(playerName, actions);
        }
    }
}
