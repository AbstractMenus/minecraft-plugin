package ru.abstractmenus.data.actions;

import ru.abstractmenus.datatype.TypeLocation;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;

public class ActionTeleport implements Action {

    private final TypeLocation location;

    private ActionTeleport(TypeLocation location){
        this.location = location;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if(location != null){
            player.teleport(location.getLocation(player, menu));
        }
    }

    public static class Serializer implements NodeSerializer<ActionTeleport>{

        @Override
        public ActionTeleport deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionTeleport(node.getValue(TypeLocation.class));
        }

    }
}
