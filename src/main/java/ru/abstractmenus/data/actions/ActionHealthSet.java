package ru.abstractmenus.data.actions;


import ru.abstractmenus.datatype.TypeDouble;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;

public class ActionHealthSet implements Action {

    private final TypeDouble health;

    private ActionHealthSet(TypeDouble health){
        this.health = health;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        player.setHealth(health.getDouble(player, menu));
    }

    public static class Serializer implements NodeSerializer<ActionHealthSet> {

        @Override
        public ActionHealthSet deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionHealthSet(node.getValue(TypeDouble.class));
        }

    }
}
