package ru.abstractmenus.data.actions;

import ru.abstractmenus.data.properties.ItemProps;
import ru.abstractmenus.datatype.TypeSlot;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.Types;
import ru.abstractmenus.menu.item.InventoryItem;
import ru.abstractmenus.api.inventory.slot.SlotIndex;
import ru.abstractmenus.api.inventory.Slot;

import java.util.*;

public class ActionPropertySet implements Action {

    private final TypeSlot slot;
    private final Map<String, ItemProperty> properties;

    private ActionPropertySet(TypeSlot slot, Map<String, ItemProperty> properties) {
        this.slot = slot;
        this.properties = properties;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        Slot slot;

        if (this.slot != null) {
            slot = this.slot.getSlot(player, menu);
        } else {
            slot = ((InventoryItem)clickedItem).getSlot(player, menu);
        }

        slot.getSlots(index -> {
            // Just modifying ItemStack from inventory won't work because remProperty breaks it
            Item item = menu.getItem(index);
            item.removeProperty(ItemProps.BINDINGS);
            item.setProperties(properties);
            menu.setItem(SlotIndex.of(index), item, player);
        });
    }

    public static class Serializer implements NodeSerializer<ActionPropertySet> {

        @Override
        public ActionPropertySet deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            TypeSlot targetSlot = null;
            Map<String, ItemProperty> propertyMap = new HashMap<>();
            Map<String, ConfigNode> children = node.childrenMap();

            for (Map.Entry<String, ConfigNode> entry : children.entrySet()){
                String key = entry.getKey();
                Class<? extends ItemProperty> token = Types.getItemPropertyType(key);

                if (token != null){
                    propertyMap.put(key, entry.getValue().getValue(token));
                }
            }

            if (node.node("slot").rawValue() != null){
                targetSlot = node.node("slot").getValue(TypeSlot.class);
            }

            return new ActionPropertySet(targetSlot, propertyMap);
        }

    }
}
