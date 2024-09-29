package ru.abstractmenus.menu.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;

import java.util.*;

public class SimpleItem implements Item {

    private Map<String, ItemProperty> materialProps;
    private Map<String, ItemProperty> simpleProps;
    private Map<String, ItemProperty> allProps = new LinkedHashMap<>();

    @Override
    public Map<String, ItemProperty> getProperties() {
        return allProps;
    }

    @Override
    public void addProperty(String key, ItemProperty property) {
        String lowerKey = key.toLowerCase();

        if (property.canReplaceMaterial()) {
            if (materialProps == null) materialProps = new LinkedHashMap<>();
            materialProps.put(lowerKey, property);
        } else {
            if (simpleProps == null) simpleProps = new LinkedHashMap<>();
            simpleProps.put(lowerKey, property);
        }

        allProps.put(lowerKey, property);
    }

    @Override
    public void setProperties(Map<String, ItemProperty> properties) {
        for (Map.Entry<String, ItemProperty> entry : properties.entrySet()) {
            addProperty(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public ItemProperty removeProperty(String key) {
        String lowerKey = key.toLowerCase();
        ItemProperty prop = allProps.remove(lowerKey);

        if (prop == null) return null;

        Map<String, ItemProperty> specificMap = prop.canReplaceMaterial() ? materialProps : simpleProps;
        specificMap.remove(lowerKey);
        return prop;
    }

    @Override
    public ItemStack build(Player player, Menu menu) {
        try {
            ItemStack item = new ItemStack(Material.STONE);

            // Apply material property first
            if (materialProps != null)
                applyProperties(materialProps.values(), item, player, menu);

            //Apply other properties
            if (simpleProps != null)
                applyProperties(simpleProps.values(), item, player, menu);

            return item;
        } catch (Exception e) {
            Logger.warning("Error while building ItemStack: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static void applyProperties(Collection<ItemProperty> props, ItemStack item, Player player, Menu menu) {
        for (ItemProperty property : props) {
            if (property.isApplyMeta()) {
                ItemMeta meta = item.getItemMeta();
                property.apply(item, meta, player, menu);
                item.setItemMeta(meta);
            } else {
                property.apply(item, item.getItemMeta(), player, menu);
            }
        }
    }

    @Override
    public boolean isSimilar(ItemStack item, Player player){
        if (item == null) return false;
        return item.isSimilar(build(player, null));
    }

    @Override
    public SimpleItem clone() {
        try {
            SimpleItem item = (SimpleItem) super.clone();
            item.allProps = new LinkedHashMap<>(item.allProps);

            if (item.materialProps != null) {
                item.materialProps = new LinkedHashMap<>(item.materialProps);
            }

            if (item.simpleProps != null) {
                item.simpleProps = new LinkedHashMap<>(item.simpleProps);
            }

            return item;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SimpleItem) {
            return Objects.equals(this.allProps, ((SimpleItem) o).allProps);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.allProps);
    }

    @Override
    public String toString(){
        return "SimpleItem:{allProps: "+allProps+", simpleProps: "+simpleProps+"}";
    }

}
