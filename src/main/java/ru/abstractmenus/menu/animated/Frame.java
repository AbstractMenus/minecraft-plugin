package ru.abstractmenus.menu.animated;


import ru.abstractmenus.data.Actions;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Slot;
import ru.abstractmenus.menu.item.InventoryItem;
import ru.abstractmenus.menu.item.MenuItem;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.data.rules.logical.RuleAnd;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Frame {

    private final long delay;
    private final boolean clear;

    private Rule rules;
    private Actions startActions;
    private Actions endActions;

    private List<Item> items;

    private Frame(long delay, boolean clear){
        this.delay = delay;
        this.clear = clear;
    }

    public Map<Integer, Item> play(Player player, Menu menu){
        if(items != null && !items.isEmpty()){
            if(rules != null && !rules.check(player, menu, null)) return null;

            Map<Integer, Item> allowedItems = new HashMap<>();

            for(Item i : items){
                if(i != null){
                    Item item = i.clone();

                    if(item instanceof MenuItem && !((MenuItem) item).checkShowRules(player, menu)) continue;

                    try{
                        if(item instanceof InventoryItem){
                            ItemStack built = item.build(player, menu);
                            if (built.getAmount() > 0){
                                Slot slot = ((InventoryItem) item).getSlot(player, menu);
                                slot.getSlots((s)->allowedItems.put(s, item));
                            }
                        }
                    } catch (Exception e){
                        Logger.severe("Cannot play frame in animated menu: " + e.getMessage());
                    }
                }
            }

            return allowedItems;
        }

        return null;
    }

    public long getDelay(){
        return delay;
    }

    public boolean isClear(){
        return clear;
    }

    public List<Item> getItems(){
        return items;
    }

    public Actions getStartActions() {
        return startActions;
    }

    public Actions getEndActions() {
        return endActions;
    }

    public void setRules(Rule rules){
        this.rules = rules;
    }

    public void setStartActions(Actions actions){
        this.startActions = actions;
    }

    public void setEndActions(Actions actions){
        this.endActions = actions;
    }

    public void setItems(List<Item> items){
        this.items = items;
    }

    public static class Serializer implements NodeSerializer<Frame> {

        @Override
        public Frame deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            int delay = node.node("delay").getInt(20);
            boolean clear = node.node("clear").getBoolean(true);
            Rule rules = node.node("rules").getValue(RuleAnd.class);
            Actions startActions = node.node("onStart").getValue(Actions.class);
            Actions endActions = node.node("onEnd").getValue(Actions.class);
            List<Item> items = new ArrayList<>(node.node("items").getList(Item.class));

            Frame frame = new Frame(delay, clear);

            frame.setRules(rules);
            frame.setStartActions(startActions);
            frame.setEndActions(endActions);
            frame.setItems(items);

            return frame;
        }

    }
}
