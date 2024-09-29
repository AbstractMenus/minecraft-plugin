package ru.abstractmenus.data.actions;


import ru.abstractmenus.datatype.TypeBool;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;

public class ActionInventoryClear implements Action {

    private final TypeBool clear;

    private ActionInventoryClear(TypeBool clear){
        this.clear = clear;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if (clear.getBool(player, menu))
            player.getInventory().clear();
    }

    public static class Serializer implements NodeSerializer<ActionInventoryClear> {

        @Override
        public ActionInventoryClear deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionInventoryClear(node.getValue(TypeBool.class));
        }

    }
}
