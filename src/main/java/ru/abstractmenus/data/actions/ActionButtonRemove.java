package ru.abstractmenus.data.actions;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.datatype.TypeSlot;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.menu.AbstractMenu;

public class ActionButtonRemove implements Action {

    private final TypeSlot slots;

    public ActionButtonRemove(TypeSlot slots) {
        this.slots = slots;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        AbstractMenu am = (AbstractMenu) menu;
        
        slots.getSlot(player, menu).getSlots(am::removeMenuItem);
    }

    public static class Serializer implements NodeSerializer<ActionButtonRemove> {
        @Override
        public ActionButtonRemove deserialize(Class<ActionButtonRemove> type, ConfigNode node) throws NodeSerializeException {
            return new ActionButtonRemove(node.getValue(TypeSlot.class));
        }
    }

}
