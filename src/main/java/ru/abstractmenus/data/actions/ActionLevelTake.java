package ru.abstractmenus.data.actions;


import ru.abstractmenus.datatype.TypeInt;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;

public class ActionLevelTake implements Action {

    private final TypeInt level;

    private ActionLevelTake(TypeInt level){
        this.level = level;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        Handlers.getLevelHandler().takeLevel(player, level.getInt(player, menu));
    }

    public static class Serializer implements NodeSerializer<ActionLevelTake> {

        @Override
        public ActionLevelTake deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionLevelTake(node.getValue(TypeInt.class));
        }

    }
}
