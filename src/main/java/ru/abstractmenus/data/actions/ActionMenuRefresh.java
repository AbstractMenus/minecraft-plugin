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

public class ActionMenuRefresh implements Action {

    private TypeBool isRefresh;
    private TypeInt ticks;

    private ActionMenuRefresh(TypeBool value){
        this.isRefresh = value;
    }

    private ActionMenuRefresh(TypeInt ticks){
        this.ticks = ticks;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if(ticks != null){
            BukkitTasks.runTaskLater(()-> MenuManager.instance().refreshMenu(player), ticks.getInt(player, menu));
            return;
        }

        boolean isRefresh = this.isRefresh.getBool(player, menu);

        if(isRefresh){
            MenuManager.instance().refreshMenu(player);
        }
    }

    public static class Serializer implements NodeSerializer<ActionMenuRefresh>{

        @Override
        public ActionMenuRefresh deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            try{
                return new ActionMenuRefresh(node.getValue(TypeInt.class));
            } catch (Exception e){
                return new ActionMenuRefresh(node.getValue(TypeBool.class));
            }
        }

    }
}
