package ru.abstractmenus.data.properties;

import ru.abstractmenus.data.BookData;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.Handlers;

import java.util.List;

public class PropBookData implements ItemProperty {

    private final BookData data;

    private PropBookData(BookData data) {
        this.data = data;
    }

    @Override
    public boolean canReplaceMaterial() {
        return false;
    }

    @Override
    public boolean isApplyMeta() {
        return true;
    }

    @Override
    public void apply(ItemStack itemStack, ItemMeta meta, Player player, Menu menu) {
        if (meta instanceof BookMeta) {
            String author = Handlers.getPlaceholderHandler().replace(player, data.getAuthor());
            String title = Handlers.getPlaceholderHandler().replace(player, data.getTitle());
            List<String> pages = Handlers.getPlaceholderHandler().replace(player, data.getPages());

            ((BookMeta) meta).setAuthor(author);
            ((BookMeta) meta).setTitle(title);
            ((BookMeta) meta).setPages(pages);
        }
    }

    public static class Serializer implements NodeSerializer<PropBookData> {

        @Override
        public PropBookData deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropBookData(node.getValue(BookData.class));
        }

    }
}
