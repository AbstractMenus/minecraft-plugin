package ru.abstractmenus.data.actions;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.datatype.TypeInt;
import ru.abstractmenus.datatype.TypeSlot;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.menu.AbstractMenu;

/**
 * removePlaced: [slot]
 * removePlaced: {
 *     slot: [slot]
 *     count: [int]
 * }
 */
public class ActionPlacedItemRemove implements Action {

    private final TypeSlot slot;
    private final TypeInt amount;

    public ActionPlacedItemRemove(TypeSlot slot, TypeInt amount) {
        this.slot = slot;
        this.amount = amount;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        AbstractMenu am = (AbstractMenu) menu;
        int amount = this.amount.getInt(player, menu);

        slot.getSlot(player, menu).getSlots(i ->
                am.removePlacedItem(i, amount));
    }

    public static class Serializer implements NodeSerializer<ActionPlacedItemRemove> {
        @Override
        public ActionPlacedItemRemove deserialize(Class<ActionPlacedItemRemove> type, ConfigNode node)
                throws NodeSerializeException {
            if (node.isPrimitive()) {
                return new ActionPlacedItemRemove(node.getValue(TypeSlot.class), new TypeInt(Integer.MAX_VALUE));
            } else if (node.isMap()) {
                TypeSlot slot = node.node("slot").getValue(TypeSlot.class);
                TypeInt amount = node.node("count").getValue(TypeInt.class);

                return new ActionPlacedItemRemove(slot, amount);
            }

            throw new NodeSerializeException(node, "Invalid action format");
        }
    }

}
