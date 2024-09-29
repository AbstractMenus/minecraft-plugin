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

public class ActionXpTake implements Action {

    private final TypeInt xp;

    private ActionXpTake(TypeInt xp){
        this.xp = xp;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        Handlers.getLevelHandler().takeXp(player, xp.getInt(player, menu));
    }

    public static class Serializer implements NodeSerializer<ActionXpTake> {

        @Override
        public ActionXpTake deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionXpTake(node.getValue(TypeInt.class));
        }

    }
}
