package ru.abstractmenus.data.actions;

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
import ru.abstractmenus.api.inventory.slot.SlotIndex;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActionPropertyRemove implements Action {

    private final TypeSlot slot;
    private final Set<String> keys;

    private ActionPropertyRemove(TypeSlot slot, Set<String> keys){
        this.slot = slot;
        this.keys = keys;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        Slot slot;

        if (this.slot != null) {
            slot = this.slot.getSlot(player, menu);
        } else {
            slot = ((InventoryItem)clickedItem).getSlot(player, menu);
        }

        slot.getSlots(s -> {
            Item item = menu.getItem(s);

            for (String key : keys) {
                item.removeProperty(key);
            }

            menu.setItem(SlotIndex.of(s), item, player);
        });
    }

    public static class Serializer implements NodeSerializer<ActionPropertyRemove> {

        @Override
        public ActionPropertyRemove deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            TypeSlot targetSlot = null;
            List<String> props;

            if (node.isMap()){
                props = node.node("properties").getList(String.class);

                if (node.node("slot").rawValue() != null){
                    targetSlot = node.node("slot").getValue(TypeSlot.class);
                }
            } else {
                props = node.getList(String.class);
            }

            return new ActionPropertyRemove(targetSlot, new HashSet<>(props));
        }
    }
}
