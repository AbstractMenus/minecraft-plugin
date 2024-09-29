package ru.abstractmenus.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.inventory.Slot;
import ru.abstractmenus.menu.item.MenuItem;
import ru.abstractmenus.menu.item.InventoryItem;
import ru.abstractmenus.api.inventory.slot.SlotIndex;

import java.util.*;

public class SimpleMenu extends AbstractMenu {

    private List<Item> items = new ArrayList<>();

    public SimpleMenu(String title, int size) {
        super(title, size);
    }

    @Override
    public Collection<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    @Override
    public boolean open(Player player) {
        if (checkOpenRules(player)) {
            openListener.onOpen(player, this);

            if (preOpenActions != null)
                preOpenActions.activate(player, this, null);

            createInventory(player, this);

            if(openActions != null)
                openActions.activate(player, this, null);

            refresh(player);

            player.openInventory(inventory);

            if (postOpenActions != null)
                postOpenActions.activate(player, this, null);
            return true;
        }

        if(denyActions != null)
            denyActions.activate(player, this, null);

        return false;
    }

    @Override
    public void refresh(Player player) {
        showedItems.clear();
        inventory.clear();
        placeItems(player);
    }

    protected void placeItems(Player player) {
        for (Item i : items) {
            if (i == null) continue;

            Item item = i.clone();

            if (item instanceof MenuItem && !((MenuItem) item).checkShowRules(player, this)) continue;

            if (item instanceof InventoryItem) {
                try {
                    ItemStack built = item.build(player, this);

                    if (built.getAmount() > 0) {
                        Slot slot = ((InventoryItem) item).getSlot(player, this);

                        if (slot instanceof SlotIndex && ((SlotIndex)slot).getIndex() == -1) {
                            slot = new SlotIndex(getFreeSlot());
                        }

                        slot.getSlots(s -> {
                            if (s >= 0 && s < inventory.getSize()) {
                                showedItems.put(s, item);
                                inventory.setItem(s, built);
                            }
                        });
                    }
                } catch (Exception e) {
                    Logger.severe("Cannot refresh menu item: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public SimpleMenu clone() {
        return (SimpleMenu) super.clone();
    }

}
