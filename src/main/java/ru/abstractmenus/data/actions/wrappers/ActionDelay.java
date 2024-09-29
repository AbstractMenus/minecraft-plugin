package ru.abstractmenus.data.actions.wrappers;


import ru.abstractmenus.data.Actions;
import ru.abstractmenus.datatype.TypeInt;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.util.bukkit.BukkitTasks;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;

public class ActionDelay implements Action {

    private final TypeInt delay;
    private final Actions actions;

    private ActionDelay(TypeInt delay, Actions actions){
        this.delay = delay;
        this.actions = actions;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if(actions != null){
            BukkitTasks.runTaskLater(()->actions.activate(player, menu, clickedItem), delay.getInt(player, menu));
        }
    }

    public static class Serializer implements NodeSerializer<ActionDelay> {

        @Override
        public ActionDelay deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            Actions actions = node.node("actions").getValue(Actions.class);
            TypeInt delay = node.node("delay").getValue(TypeInt.class, new TypeInt(20));
            return new ActionDelay(delay, actions);
        }
    }
}
