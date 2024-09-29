package ru.abstractmenus.data.actions;


import ru.abstractmenus.datatype.TypeBool;
import ru.abstractmenus.datatype.TypeInt;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.services.MenuManager;
import ru.abstractmenus.util.bukkit.BukkitTasks;

public class ActionMenuClose implements Action {

    private TypeBool isClose;
    private TypeInt ticks = null;

    private ActionMenuClose(TypeBool value){
        this.isClose = value;
    }

    private ActionMenuClose(TypeInt ticks){
        this.ticks = ticks;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if(ticks != null) {
            BukkitTasks.runTaskLater(() ->
                    MenuManager.instance().closeMenu(player),
                    ticks.getInt(player, menu));
        } else {
            if (isClose.getBool(player, menu))
                MenuManager.instance().closeMenu(player);
        }
    }

    public static class Serializer implements NodeSerializer<ActionMenuClose> {

        @Override
        public ActionMenuClose deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            try{
                return new ActionMenuClose(node.getValue(TypeInt.class));
            } catch (Exception e){
                return new ActionMenuClose(node.getValue(TypeBool.class));
            }
        }

    }
}
