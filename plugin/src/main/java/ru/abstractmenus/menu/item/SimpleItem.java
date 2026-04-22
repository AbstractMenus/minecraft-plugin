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

    /**
     * Copy-on-write flag. {@code true} for a freshly-constructed (template) item — the
     * three property maps may be mutated in-place. Set to {@code false} by {@link #clone()}
     * so the clone shares the template's map references; the next mutation triggers a real
     * copy via {@link #ensureOwnedProps()}.
     *
     * <p>Most refresh ticks never mutate properties — for those clones we now skip three
     * {@code LinkedHashMap} allocations per item per refresh (54 items × 100 players ×
     * 20 TPS ≈ 324 000 maps/sec previously).
     */
    private boolean propsOwned = true;

    @Override
    public Map<String, ItemProperty> getProperties() {
        return allProps;
    }

    @Override
    public void addProperty(String key, ItemProperty property) {
        ensureOwnedProps();
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
        ensureOwnedProps();
        String lowerKey = key.toLowerCase();
        ItemProperty prop = allProps.remove(lowerKey);

        if (prop == null) return null;

        Map<String, ItemProperty> specificMap = prop.canReplaceMaterial() ? materialProps : simpleProps;
        specificMap.remove(lowerKey);
        return prop;
    }

    private void ensureOwnedProps() {
        if (propsOwned) return;
        allProps = new LinkedHashMap<>(allProps);
        if (materialProps != null) materialProps = new LinkedHashMap<>(materialProps);
        if (simpleProps != null) simpleProps = new LinkedHashMap<>(simpleProps);
        propsOwned = true;
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
        ItemMeta meta = null;

        for (ItemProperty property : props) {
            if (property.isApplyMeta()) {
                if (meta == null) meta = item.getItemMeta();
                property.apply(item, meta, player, menu);
            } else {
                // Flush pending meta before non-meta property (may change item type)
                if (meta != null) {
                    item.setItemMeta(meta);
                    meta = null;
                }
                property.apply(item, item.getItemMeta(), player, menu);
            }
        }

        if (meta != null) {
            item.setItemMeta(meta);
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
            // Share map references with the template; ensureOwnedProps() will lazy-copy
            // on the first add/remove/setProperties call. ItemProperty.apply() never
            // mutates these maps, so common refresh paths perform zero allocations here.
            item.propsOwned = false;
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
