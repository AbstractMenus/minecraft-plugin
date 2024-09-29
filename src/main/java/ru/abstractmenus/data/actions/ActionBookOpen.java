package ru.abstractmenus.data.actions;

import ru.abstractmenus.api.handler.PlaceholderHandler;
import ru.abstractmenus.data.BookData;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.nms.book.Book;
import ru.abstractmenus.util.NMS;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Handlers;

public class ActionBookOpen extends NMS implements Action {

    private final BookData bookData;

    private ActionBookOpen(BookData bookData){
        this.bookData = bookData;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        BookData data = bookData.clone();
        PlaceholderHandler handler = Handlers.getPlaceholderHandler();

        data.setAuthor(handler.replace(player, bookData.getAuthor()));
        data.setTitle(handler.replace(player, bookData.getTitle()));
        data.setPages(handler.replace(player, bookData.getPages()));

        Book book = Book.create(data);
        book.open(player);
    }

    public static class Serializer implements NodeSerializer<ActionBookOpen> {

        @Override
        public ActionBookOpen deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionBookOpen(node.getValue(BookData.class));
        }

    }
}
