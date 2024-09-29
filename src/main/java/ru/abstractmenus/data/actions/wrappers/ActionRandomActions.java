package ru.abstractmenus.data.actions.wrappers;

import ru.abstractmenus.data.Actions;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ActionRandomActions implements Action {

    private final List<Actions> actions;

    private ActionRandomActions(List<Actions> actions){
        this.actions = actions;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        int index = ThreadLocalRandom.current().nextInt(actions.size());
        Actions actions = this.actions.get(index);

        if (actions != null){
            actions.activate(player, menu, clickedItem);
        }
    }

    public static class Serializer implements NodeSerializer<ActionRandomActions> {

        @Override
        public ActionRandomActions deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionRandomActions(node.getList(Actions.class));
        }

    }
}
