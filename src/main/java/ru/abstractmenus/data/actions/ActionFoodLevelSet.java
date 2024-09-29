package ru.abstractmenus.data.actions;


import ru.abstractmenus.datatype.TypeInt;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;

public class ActionFoodLevelSet implements Action {

    private final TypeInt foodLevel;

    private ActionFoodLevelSet(TypeInt foodLevel){
        this.foodLevel = foodLevel;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        player.setFoodLevel(foodLevel.getInt(player, menu));
    }

    public static class Serializer implements NodeSerializer<ActionFoodLevelSet> {

        @Override
        public ActionFoodLevelSet deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionFoodLevelSet(node.getValue(TypeInt.class));
        }

    }
}
