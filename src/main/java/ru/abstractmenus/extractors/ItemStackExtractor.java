package ru.abstractmenus.extractors;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.util.bukkit.ItemUtil;

public class ItemStackExtractor implements ValueExtractor {

    public static final ItemStackExtractor INSTANCE = new ItemStackExtractor();

    @Override
    public String extract(Object obj, String placeholder) {
        if (obj instanceof ItemStack) {
            ItemStack item = (ItemStack) obj;

            switch (placeholder) {
                case "item_type": return item.getType().toString();
                case "item_data": return String.valueOf(item.getData().getData()); // 1.12-
                case "item_amount": return String.valueOf(item.getAmount());
                case "item_max_stack": return String.valueOf(item.getMaxStackSize());
                case "item_serialized": return ItemUtil.encodeStack(item);
            }

            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                switch (placeholder) {
                    case "item_display_name": return meta.getDisplayName();
                    case "item_localized_name": return meta.getLocalizedName();
                    case "item_model": return String.valueOf(meta.getCustomModelData()); // 1.14+
                }
            }
        }

        return "";
    }

}
