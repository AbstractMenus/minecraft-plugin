package ru.abstractmenus.data.actions;


import ru.abstractmenus.datatype.TypeBool;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;

public class ActionSkinReset implements Action {

    private final TypeBool reset;

    private ActionSkinReset(TypeBool reset){
        this.reset = reset;
    }

    public void activate(Player player, Menu menu, Item clickedItem){
        if (reset.getBool(player, menu)){
            Handlers.getSkinHandler().resetSkin(player);
        }
    }

    public static class Serializer implements NodeSerializer<ActionSkinReset> {

        @Override
        public ActionSkinReset deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionSkinReset(node.getValue(TypeBool.class));
        }

    }

}
