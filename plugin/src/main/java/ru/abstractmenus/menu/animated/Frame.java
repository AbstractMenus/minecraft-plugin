package ru.abstractmenus.menu.animated;


import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
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

    @Getter
    private final long delay;
    @Getter
    private final boolean clear;

    @Setter
    private Rule rules;
    @Setter
    @Getter
    private Actions startActions;
    @Setter
    @Getter
    private Actions endActions;

    @Setter
    @Getter
    private List<Item> items;

    private Frame(long delay, boolean clear) {
        this.delay = delay;
        this.clear = clear;
    }

    /**
     * Output of {@link #play(Player, Menu)} per slot — pairs the menu Item (used
     * for the click registry) with the already-built ItemStack (placed straight
     * into the inventory). Carrying both lets AnimatedMenu skip a second
     * {@code item.build(...)} pass, which used to double the work for every
     * animation frame.
     */
    public record PlayedSlot(Item item, ItemStack stack) {}

    public Map<Integer, PlayedSlot> play(Player player, Menu menu) {
        if (items == null || items.isEmpty()) return null;
        if (rules != null && !rules.check(player, menu, null)) return null;

        Map<Integer, PlayedSlot> allowedItems = new HashMap<>();

        for (Item i : items) {
            if (i == null) continue;
            Item item = i.clone();

            if (item instanceof MenuItem && !((MenuItem) item).checkShowRules(player, menu)) continue;
            if (!(item instanceof InventoryItem inventoryItem)) continue;

            try {
                ItemStack built = item.build(player, menu);
                if (built == null) continue;
                if (built.getAmount() > 0 || Material.AIR.equals(built.getType())) {
                    Slot slot = inventoryItem.getSlot(player, menu);
                    PlayedSlot played = new PlayedSlot(item, built);
                    slot.getSlots((s) -> allowedItems.put(s, played));
                }
            } catch (Exception e) {
                Logger.severe("Cannot play frame in animated menu: " + e.getMessage());
            }
        }

        return allowedItems;
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
