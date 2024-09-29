package ru.abstractmenus.data.actions;

import ru.abstractmenus.datatype.TypeBool;
import ru.abstractmenus.datatype.TypeInt;
import ru.abstractmenus.datatype.TypeSlot;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Slot;
import ru.abstractmenus.menu.item.InventoryItem;
import ru.abstractmenus.util.bukkit.BukkitTasks;

public class ActionItemRefresh implements Action {

    private final TypeBool isRefresh;
    private final TypeInt ticks;
    private final TypeSlot slot;

    private ActionItemRefresh(TypeBool value, TypeInt ticks, TypeSlot slot){
        this.isRefresh = value;
        this.ticks = ticks;
        this.slot = slot;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        Slot slot = ((InventoryItem) clickedItem).getSlot(player, menu);

        if (this.slot != null) {
            slot = this.slot.getSlot(player, menu);
        }

        if (ticks != null) {
            Slot finalSlot = slot;
            BukkitTasks.runTaskLater(() ->
                    menu.refreshItem(finalSlot, player), ticks.getInt(player, menu)
            );
            return;
        }

        if (isRefresh != null) {
            if (!this.isRefresh.getBool(player, menu)) return;
        }

        menu.refreshItem(slot, player);
    }

    public static class Serializer implements NodeSerializer<ActionItemRefresh>{

        @Override
        public ActionItemRefresh deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            TypeInt ticks = null;
            TypeBool isRefresh = null;
            TypeSlot slot = null;

            if (node.isPrimitive()){
                try{
                    ticks = node.getValue(TypeInt.class);
                } catch (Exception e){
                    isRefresh = node.getValue(TypeBool.class);
                }
            } else if (node.isMap()){
                ticks = node.node("delay").getValue(TypeInt.class);
                slot = node.node("slot").getValue(TypeSlot.class);
            } else {
                throw new NodeSerializeException(node, "Cannot load actions 'refreshItem'. Invalid format.");
            }

            return new ActionItemRefresh(isRefresh, ticks, slot);
        }

    }
}
