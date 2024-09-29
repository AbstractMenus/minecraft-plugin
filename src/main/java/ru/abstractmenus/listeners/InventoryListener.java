package ru.abstractmenus.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Slot;
import ru.abstractmenus.menu.AbstractMenu;
import ru.abstractmenus.services.MenuManager;
import ru.abstractmenus.util.SlotUtil;
import ru.abstractmenus.util.bukkit.BukkitTasks;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlot() < 0
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
            ItemStack old = event.getCurrentItem();
            ItemStack cursor = event.getCursor();

            switch (event.getAction()) {
                case PICKUP_ALL:
                    if (!menu.takeItem(player, event.getSlot(), 0))
                        event.setCancelled(true);
                    break;
                case PICKUP_HALF:
                    int amount = (int) Math.ceil((float) old.getAmount() / 2);
                    if (!menu.takeItem(player, event.getSlot(), amount))
                        event.setCancelled(true);
                    break;
                case PICKUP_ONE:
                    if (!menu.takeItem(player, event.getSlot(), 1))
                        event.setCancelled(true);
                    break;
                case PLACE_ALL:
                case PLACE_SOME:
                    menu.placeItem(player, event.getSlot(), calcPlaced(old, cursor, 0));
                    break;
                case PLACE_ONE:
                    menu.placeItem(player, event.getSlot(), calcPlaced(old, cursor, 1));
                    break;
                case SWAP_WITH_CURSOR:
                    if (menu.takeItem(player, event.getSlot(), 0)) {
                        menu.placeItem(player, event.getSlot(), cursor);
                    } else {
                        event.setCancelled(true);
                    }
                    break;
                default:
                    break;
            }
        } else {
            event.setCancelled(true);
        }

        menu.click(event.getSlot(), player, event.getClick());
        BukkitTasks.runTaskLater(player::updateInventory, 2L);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player
                && event.getInventory().getHolder() instanceof Menu) {

            Player player = (Player) event.getWhoClicked();
            AbstractMenu menu = (AbstractMenu) event.getInventory().getHolder();
            Slot draggable = menu.getDraggableSlots(player);
            Collection<Integer> slots = SlotUtil.collect(draggable);

            boolean hasPlayerSlots = false;
            boolean hasMenuSlots = false;

            for (int slot : event.getRawSlots()) {
                if (slot >= menu.getSize()) {
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

            for (Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
                menu.placeItem(player, entry.getKey(), entry.getValue());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player && event.getInventory().getHolder() instanceof Menu) {
            Player player = (Player) event.getPlayer();
            Menu closed = (Menu) event.getInventory().getHolder();
            Menu current = MenuManager.instance().getOpenedMenu(player);

            if (closed instanceof AbstractMenu) {
                ((AbstractMenu)closed).dropPlaced(player);
            }

            if (current != null && current.equals(closed)) {
                BukkitTasks.runTaskLater(
                        () -> MenuManager.instance().closeMenu(player, false),
                        3L
                );
            }
        }
    }

    private static ItemStack calcPlaced(ItemStack current, ItemStack cursor, int modifier) {
        ItemStack stack;
        int amount;

        if (current != null && current.getType() != Material.AIR) {
            stack = current.clone();
            int mod = modifier > 0 ? modifier : cursor.getAmount();
            amount = stack.getAmount() + mod;
        } else {
            stack = cursor.clone();
            amount = modifier > 0 ? modifier : cursor.getAmount();
        }

        amount = Math.min(amount, stack.getMaxStackSize());
        stack.setAmount(amount);
        return stack;
    }
}
