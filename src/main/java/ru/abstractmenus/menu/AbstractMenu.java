package ru.abstractmenus.menu;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.datatype.TypeSlot;
import ru.abstractmenus.menu.item.MenuItem;
import ru.abstractmenus.data.Actions;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Slot;
import ru.abstractmenus.util.MiniMessageUtil;
import ru.abstractmenus.util.SlotUtil;
import ru.abstractmenus.util.TimeUtil;
import ru.abstractmenus.util.bukkit.Events;
import ru.abstractmenus.util.bukkit.ItemUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractMenu implements Menu {

    protected final String title;
    protected int size;
    protected InventoryType type;

    private List<Activator> activators;
    private Activator activatedBy;
    private Object context;

    private long updateInterval = -1;
    private long lastUpdate;

    protected Rule openRules;
    protected Actions denyActions;
    protected Actions preOpenActions;
    protected Actions openActions;
    protected Actions postOpenActions;
    protected Actions closeActions;
    protected Actions updateActions;

    protected Inventory inventory;
    protected Map<Integer, Item> showedItems;

    protected MenuListener openListener;

    protected TypeSlot draggableSlots;
    protected Map<Integer, ItemStack> placedItems;
    protected ItemStack lastPlaced = ItemUtil.empty();
    protected ItemStack lastTaken = ItemUtil.empty();
    protected ItemStack lastItem = ItemUtil.empty();
    protected int lastPlacedSlot = -1;
    protected int lastTakenSlot = -1;
    protected Actions onPlaceItem;
    protected Actions onTakeItem;
    protected Actions onDragItem;

    public AbstractMenu(String title, int size){
        this.title = title;
        this.size = size;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public Item getItem(int slot) {
        return showedItems == null ? null : showedItems.get(slot);
    }

    @Override
    public void setItem(Slot slot, Item item, Player player) {
        slot.getSlots(index -> {
            showedItems.put(index, item);
            inventory.setItem(index, item.build(player, this));

            if (placedItems != null)
                placedItems.remove(index);
        });
    }

    public void removeMenuItem(int slot) {
        showedItems.remove(slot);
        inventory.clear(slot);
    }

    @Override
    public void refreshItem(Slot slot, Player player) {
        if (inventory != null) {
            slot.getSlots((s) -> {
                Item item = getItem(s);

                if (item != null) {
                    if (item instanceof MenuItem && !((MenuItem)item).checkShowRules(player, this))
                        return;

                    inventory.setItem(s, item.build(player, this));
                }
            });
        }
    }

    @Override
    public List<Activator> getActivators() {
        return activators;
    }

    @Override
    public Optional<Activator> getActivatedBy() {
        return Optional.ofNullable(activatedBy);
    }

    @Override
    public Optional<Object> getContext() {
        return Optional.ofNullable(context);
    }

    @Nullable
    public Slot getDraggableSlots(Player player) {
        if (draggableSlots == null) return null;
        return draggableSlots.getSlot(player, this);
    }

    public void setDraggableSlots(TypeSlot draggableSlots) {
        this.draggableSlots = draggableSlots;
    }

    public boolean isDraggable(Player player, int slotIndex) {
        if (draggableSlots == null) return false;
        Slot slot = draggableSlots.getSlot(player, this);
        return SlotUtil.contains(slot, slotIndex);
    }

    public void setOnPlaceItem(Actions onPlaceItem) {
        this.onPlaceItem = onPlaceItem;
    }

    public void setOnTakeItem(Actions onTakeItem) {
        this.onTakeItem = onTakeItem;
    }

    public void setOnDragItem(Actions onDragItem) {
        this.onDragItem = onDragItem;
    }

    public ItemStack getLastPlaced() {
        return lastPlaced;
    }

    public ItemStack getLastTaken() {
        return lastTaken;
    }

    public ItemStack getLastMovedItem() {
        return lastItem;
    }

    public int getLastPlacedSlot() {
        return lastPlacedSlot;
    }

    public int getLastTakenSlot() {
        return lastTakenSlot;
    }

    public ItemStack getPlacedItem(int slot) {
        if (placedItems == null) return ItemUtil.empty();
        return placedItems.getOrDefault(slot, ItemUtil.empty());
    }

    public void dropPlaced(Player player) {
        if (draggableSlots != null && placedItems != null) {
            Location loc = player.getEyeLocation();
            for (ItemStack item : placedItems.values()) {
                player.getWorld()
                        .dropItem(loc, item)
                        .setVelocity(loc.getDirection().multiply(0.2F));
            }
        }
    }

    public void removePlacedItem(int slot, int amount) {
        if (placedItems != null) {
            ItemStack placed = placedItems.get(slot);

            if (placed != null) {
                if (amount >= placed.getAmount()) {
                    placedItems.remove(slot);
                    inventory.clear(slot);
                } else {
                    placed.setAmount(placed.getAmount() - amount);
                    inventory.setItem(slot, placed);
                }
            }
        }
    }

    public void placeItemQuiet(Player player, Slot slot, ItemStack item) {
        Slot draggable = getDraggableSlots(player);

        if (draggable != null) {
            slot.getSlots(index -> {
                removeMenuItem(index);
                placedItems.put(index, item);
                inventory.setItem(index, item);
            });
        }
    }

    public void placeItem(Player player, int slot, ItemStack item) {
        if (placedItems == null) placedItems = new HashMap<>();

        ItemStack last = placedItems.get(slot);

        if (item.isSimilar(last)) {
            lastPlaced = last.clone();
            lastPlaced.setAmount(item.getAmount() - last.getAmount());
        } else {
            lastPlaced = item;
        }

        placedItems.put(slot, item);
        lastItem = item;
        lastPlacedSlot = slot;

        if (onPlaceItem != null)
            onPlaceItem.activate(player, this,null);

        if (onDragItem != null)
            onDragItem.activate(player, this,null);
    }

    public boolean takeItem(Player player, int slot, int amount) {
        if (placedItems == null) return false;

        ItemStack old = placedItems.get(slot);

        if (old == null) return false;

        if (amount == 0 || amount >= old.getAmount()) {
            placedItems.remove(slot);
            lastItem = ItemUtil.empty();
            lastTaken = old;
        } else {
            ItemStack taken = old.clone();
            old.setAmount(old.getAmount() - amount);
            taken.setAmount(amount);
            lastItem = old;
            lastTaken = taken;
        }

        lastTakenSlot = slot;

        if (onTakeItem != null)
            onTakeItem.activate(player, this,null);

        if (onDragItem != null)
            onDragItem.activate(player, this,null);

        return true;
    }

    public void setType(InventoryType type) {
        this.type = type;
    }

    public void setOpenListener(MenuListener openListener) {
        this.openListener = openListener;
    }

    public void setContext(Object ctx) {
        this.context = ctx;
    }

    public void setActivatedBy(Activator activator) {
        this.activatedBy = activator;
    }

    public void setActivators(List<Activator> activators) {
        for(Activator activator : activators) {
            activator.setTargetMenu(this);
            Events.register(activator);
        }
        this.activators = activators;
    }

    public void setOpenRules(Rule rules){
        this.openRules = rules;
    }

    public void setPreOpenActions(Actions preOpenActions) {
        this.preOpenActions = preOpenActions;
    }

    public void setOpenActions(Actions actions) {
        this.openActions = actions;
    }

    public void setPostOpenActions(Actions postOpenActions) {
        this.postOpenActions = postOpenActions;
    }

    public void setCloseActions(Actions actions) {
        this.closeActions = actions;
    }

    public void setDenyActions(Actions actions) {
        this.denyActions = actions;
    }

    public void setUpdateActions(Actions actions) {
        this.updateActions = actions;
    }

    public void setUpdateInterval(long interval) {
        this.updateInterval = interval;
    }

    @Override
    public void close(Player player, boolean closeInventory) {
        if(closeInventory)
            player.closeInventory();

        if(closeActions != null)
            closeActions.activate(player, this, null);
    }

    @Override
    public void update(Player player) {
        if(isReadyToUpdate(player)) {
            lastUpdate = TimeUtil.currentTimeTicks();
            if (updateActions != null)
                updateActions.activate(player, this, null);
            refresh(player);
        }
    }

    protected boolean isReadyToUpdate(Player player){
        return updateInterval != -1
                && player != null
                && player.isOnline()
                && TimeUtil.currentTimeTicks() >= (lastUpdate + updateInterval);
    }

    @Override
    public void click(int slot, Player player, ClickType type) {
        Item item = getItem(slot);

        if(item instanceof MenuItem)
            ((MenuItem) item).doClick(type, this, player);
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    protected boolean checkOpenRules(Player player) {
        return openRules == null || openRules.check(player, this, null);
    }

    protected int getFreeSlot(){
        ItemStack[] content = inventory.getContents();
        for (int i = 0; i < content.length; i++){
            if (content[i] == null) return i;
        }
        return -1;
    }

    protected void createInventory(Player player, InventoryHolder holder) {
        String title = Handlers.getPlaceholderHandler().replace(player, this.title);
        title = MiniMessageUtil.parseToLegacy(title);

        if (this.type != null) {
            this.inventory = Bukkit.createInventory(holder, type, title);
        } else {
            this.inventory = Bukkit.createInventory(holder, size, title);
        }
    }

    @Override
    public AbstractMenu clone() {
        try {
            AbstractMenu menu = (AbstractMenu) super.clone();
            menu.showedItems = new HashMap<>();
            return menu;
        } catch (CloneNotSupportedException e){
            return null;
        }
    }
}
