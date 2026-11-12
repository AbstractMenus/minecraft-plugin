package ru.abstractmenus.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Slot;
import ru.abstractmenus.menu.AbstractMenu;
import ru.abstractmenus.services.MenuManager;
import ru.abstractmenus.util.SlotUtil;
import ru.abstractmenus.util.bukkit.BukkitTasks;

import java.util.Collection;
import java.util.HashSet;

public class InventoryListener implements Listener {

    private static final Collection<InventoryAction> allowedActions;

    static {
        allowedActions = new HashSet<>();
        allowedActions.add(InventoryAction.PLACE_ONE);
        allowedActions.add(InventoryAction.PLACE_SOME);
        allowedActions.add(InventoryAction.PLACE_ALL);
        allowedActions.add(InventoryAction.PICKUP_ONE);
        allowedActions.add(InventoryAction.PICKUP_HALF);
        allowedActions.add(InventoryAction.PICKUP_ALL);
        allowedActions.add(InventoryAction.SWAP_WITH_CURSOR);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || event.getSlot() < 0
                || !(event.getInventory().getHolder() instanceof Menu)
                || !(event.getWhoClicked() instanceof Player)
                || event.getClickedInventory() == null) return;

        AbstractMenu menu = (AbstractMenu) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();

        if (!allowedActions.contains(event.getAction())) {
            event.setCancelled(true);
        }

        if (!(event.getClickedInventory().getHolder() instanceof Menu)) {
            if (menu.getDraggableSlots(player) == null)
                event.setCancelled(true);

            return;
        }

        if (menu.isDraggable(player, event.getSlot())) {
            switch (event.getAction()) {
                case PICKUP_ALL, PICKUP_HALF, PICKUP_ONE, SWAP_WITH_CURSOR:
                    if (menu.getPlacedItem(event.getSlot()).getType().isAir()) event.setCancelled(true);
                    break;
                default:
                    break;
            }
        } else {
            event.setCancelled(true);
        }

        menu.click(event.getSlot(), player, event.getClick());
        // Pin to player's region: updateInventory writes to the player's
        // inventory which on Folia is owned by the player's region thread.
        BukkitTasks.runForEntityLater(player, player::updateInventory, 2L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!event.isCancelled() && event.getWhoClicked() instanceof Player
                && event.getInventory().getHolder() instanceof Menu) {

            Player player = (Player) event.getWhoClicked();
            AbstractMenu menu = (AbstractMenu) event.getInventory().getHolder();
            Slot draggable = menu.getDraggableSlots(player);
            Collection<Integer> slots = SlotUtil.collect(draggable);

            boolean hasPlayerSlots = false;
            boolean hasMenuSlots = false;

            for (int slot : event.getRawSlots()) {
                if (slot >= event.getView().getTopInventory().getSize()) {
                    hasPlayerSlots = true;
                } else {
                    hasMenuSlots = true;
                }
            }

            if (hasPlayerSlots) {
                if (hasMenuSlots)
                    event.setCancelled(true);
                return;
            }

            if (!slots.containsAll(event.getInventorySlots())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void reconcileClick(InventoryClickEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player player)
                || !(event.getInventory().getHolder() instanceof AbstractMenu menu)
                || event.getClickedInventory() != event.getInventory()
                || !allowedActions.contains(event.getAction()) || !menu.isDraggable(player, event.getSlot())) return;
        menu.queuePlacedReconcile(player, event.getSlot(), () -> !event.isCancelled());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void reconcileDrag(InventoryDragEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player player)
                || !(event.getInventory().getHolder() instanceof AbstractMenu menu)) return;
        for (int slot : event.getRawSlots()) {
            if (slot < event.getInventory().getSize() && menu.isDraggable(player, slot)) {
                menu.queuePlacedReconcile(player, slot, () -> !event.isCancelled());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player && event.getInventory().getHolder() instanceof Menu closed) {
            MenuManager.instance().closeMenu(player, closed, false);
        }
    }
}
