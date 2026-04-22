package ru.abstractmenus.data.properties;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.datatype.TypeEnum;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.util.bukkit.ItemUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PropEquipItem implements ItemProperty {

    private final static Map<EquipmentSlot, Function<Player, ItemStack>> providers;

    static {
        providers = new HashMap<>();
        providers.put(EquipmentSlot.FEET, p -> p.getInventory().getBoots());
        providers.put(EquipmentSlot.LEGS, p -> p.getInventory().getLeggings());
        providers.put(EquipmentSlot.CHEST, p -> p.getInventory().getChestplate());
        providers.put(EquipmentSlot.HEAD, p -> p.getInventory().getHelmet());
        providers.put(EquipmentSlot.HAND, PropEquipItem::getMainHand);

        try {
            providers.put(EquipmentSlot.OFF_HAND, p -> p.getInventory().getItemInOffHand());
        } catch (Throwable ignore) {}
    }

    private final TypeEnum<EquipmentSlot> slot;
    private final String playerName;

    public PropEquipItem(TypeEnum<EquipmentSlot> slot, String playerName) {
        this.slot = slot;
        this.playerName = playerName;
    }

    @Override
    public boolean canReplaceMaterial() {
        return true;
    }

    @Override
    public boolean isApplyMeta() {
        return false;
    }

    @Override
    public void apply(ItemStack item, ItemMeta meta, Player player, Menu menu) {
        EquipmentSlot slot = this.slot.getEnum(EquipmentSlot.class, player, menu);
        Player target = player;

        if (playerName != null) {
            String replaced = Handlers.getPlaceholderHandler().replace(player, playerName);
            Player found = Bukkit.getPlayerExact(replaced);

            if (found == null || !found.isOnline()) {
                Logger.warning(String.format("Online player '%s' for equipItem property not found", replaced));
                return;
            }

            target = found;
        }

        ItemStack inventoryItem = providers.get(slot).apply(target);

        if (inventoryItem != null) {
            ItemUtil.merge(item, inventoryItem);
        }
    }

    private static ItemStack getMainHand(Player player) {
        try {
            return player.getInventory().getItemInMainHand();
        } catch (Throwable t) {
            return player.getInventory().getItemInHand();
        }
    }

    public static class Serializer implements NodeSerializer<PropEquipItem> {

        @Override
        public PropEquipItem deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if (node.isPrimitive()) {
                return new PropEquipItem(node.getValue(TypeEnum.class), null);
            } else if (node.isMap()) {
                String player = node.node("player").getString();
                TypeEnum<EquipmentSlot> slot = node.node("slot").getValue(TypeEnum.class);

                return new PropEquipItem(slot, player);
            }

            throw new NodeSerializeException(node, "Invalid equipItem property format");
        }
    }
}
