package ru.abstractmenus.menu;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.MainConfig;
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
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractMenu implements Menu {

    protected final String title;
    protected int size;
    @Setter
    protected InventoryType type;

    private List<Activator> activators;
    @Setter
    private Activator activatedBy;
    @Setter
    private Object context;

    @Setter
    private long updateInterval = -1;
    private long lastUpdate;

    @Setter
    protected Rule openRules;
    @Setter
    protected Actions denyActions;
    @Setter
    protected Actions preOpenActions;
    @Setter
    protected Actions openActions;
    @Setter
    protected Actions postOpenActions;
    @Setter
    protected Actions closeActions;
    @Setter
    protected Actions updateActions;

    protected Inventory inventory;
    protected Map<Integer, Item> showedItems;

    // Click cooldown lives on the menu (not on the per-clone MenuItem) so
    // refreshMenu cannot reset it by re-cloning items. Keyed by (slot, type)
    // so different click types on the same slot have independent windows.
    protected Map<SlotClickKey, Long> slotClickExpiry = new HashMap<>();

    // Floor on the per-click debounce window. Catches protocol-level duplicate
    // events that the configured clickCooldown is too short to absorb. The
    // vanilla Minecraft client emits ~3 extra QUICK_MOVE packets per physical
    // shift-click at ~70ms intervals, with the last one ~200ms after the
    // original; the SHIFT floor (configurable, default 250ms) catches those.
    // Plain LEFT/RIGHT use a smaller floor because the synthetic DOUBLE_CLICK
    // event Mojang piggybacks on rapid same-slot clicks is already filtered
    // semantically inside MenuItem.doClick.
    protected record SlotClickKey(int slot, ClickType type) {}

    private static long debounceFloorMs(ClickType type) {
        MainConfig conf = AbstractMenus.instance().getMainConfig();
        return (type == ClickType.SHIFT_LEFT || type == ClickType.SHIFT_RIGHT)
                ? conf.getClickDebounceShiftMs()
                : conf.getClickDebounceDefaultMs();
    }

    @Setter
    protected MenuListener openListener;

    @Setter
    protected TypeSlot draggableSlots;
    protected Map<Integer, ItemStack> placedItems = new ConcurrentHashMap<>();
    @Getter
    protected ItemStack lastPlaced = ItemUtil.empty();
    @Getter
    protected ItemStack lastTaken = ItemUtil.empty();
    protected ItemStack lastItem = ItemUtil.empty();
    @Getter
    protected int lastPlacedSlot = -1;
    @Getter
    protected int lastTakenSlot = -1;
    @Setter
    protected Actions onPlaceItem;
    @Setter
    protected Actions onTakeItem;
    @Setter
    protected Actions onDragItem;

    public AbstractMenu(String title, int size) {
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
                    if (item instanceof MenuItem && !((MenuItem) item).checkShowRules(player, this))
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

    public boolean isDraggable(Player player, int slotIndex) {
        if (draggableSlots == null) return false;
        Slot slot = draggableSlots.getSlot(player, this);
        return SlotUtil.contains(slot, slotIndex);
    }

    public ItemStack getLastMovedItem() {
        return lastItem;
    }

    public ItemStack getPlacedItem(int slot) {
        return placedItems.getOrDefault(slot, ItemUtil.empty());
    }

    public void dropPlaced(Player player) {
        if (draggableSlots != null && !placedItems.isEmpty()) {
            Location loc = player.getEyeLocation();
            for (ItemStack item : placedItems.values()) {
                player.getWorld()
                        .dropItem(loc, item)
                        .setVelocity(loc.getDirection().multiply(0.2F));
            }
            placedItems.clear();
        }
    }

    public void removePlacedItem(int slot, int amount) {
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
            onPlaceItem.activate(player, this, null);

        if (onDragItem != null)
            onDragItem.activate(player, this, null);
    }

    public boolean takeItem(Player player, int slot, int amount) {
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
            onTakeItem.activate(player, this, null);

        if (onDragItem != null)
            onDragItem.activate(player, this, null);

        return true;
    }

    public void setActivators(List<Activator> activators) {
        for (Activator activator : activators) {
            activator.setTargetMenu(this);
            Events.register(activator);
        }
        this.activators = activators;
    }

    @Override
    public void close(Player player, boolean closeInventory) {
        if (closeInventory)
            player.closeInventory();

        if (closeActions != null)
            closeActions.activate(player, this, null);
    }

    @Override
    public void update(Player player) {
        if (isReadyToUpdate(player)) {
            lastUpdate = TimeUtil.currentTimeTicks();
            if (updateActions != null)
                updateActions.activate(player, this, null);
            refresh(player);
        }
    }

    protected boolean isReadyToUpdate(Player player) {
        return updateInterval != -1
                && player != null
                && player.isOnline()
                && TimeUtil.currentTimeTicks() >= (lastUpdate + updateInterval);
    }

    @Override
    public void click(int slot, Player player, ClickType type) {
        Item item = getItem(slot);

        if (!(item instanceof MenuItem menuItem)) return;

        int cooldownTicks = menuItem.getClickCooldown();
        if (cooldownTicks > 0) {
            long now = System.currentTimeMillis();
            long cooldownMs = Math.max(debounceFloorMs(type), cooldownTicks * 50L);
            SlotClickKey key = new SlotClickKey(slot, type);
            Long expiry = slotClickExpiry.get(key);
            if (expiry != null && now < expiry) return;
            slotClickExpiry.put(key, now + cooldownMs);
        }

        menuItem.doClick(type, this, player);
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    protected boolean checkOpenRules(Player player) {
        return openRules == null || openRules.check(player, this, null);
    }

    protected int getFreeSlot() {
        ItemStack[] content = inventory.getContents();
        for (int i = 0; i < content.length; i++) {
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
            menu.placedItems = new ConcurrentHashMap<>();
            menu.slotClickExpiry = new HashMap<>();
            return menu;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
