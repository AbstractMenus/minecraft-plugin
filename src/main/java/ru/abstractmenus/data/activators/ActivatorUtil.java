package ru.abstractmenus.data.activators;

import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Objects;

public final class ActivatorUtil {

    private ActivatorUtil() { }

    public static boolean checkHand(PlayerInteractEvent event) {
        try {
            return Objects.equals(event.getHand(), EquipmentSlot.HAND);
        } catch (Throwable ignore) {
            return true;
        }
    }

    public static boolean checkHand(PlayerInteractEntityEvent event) {
        try {
            return Objects.equals(event.getHand(), EquipmentSlot.HAND);
        } catch (Throwable ignore) {
            return true;
        }
    }

}
