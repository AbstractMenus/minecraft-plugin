package ru.abstractmenus.nms.book;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.data.BookData;

public class Book_1_15 extends Book {

    Book_1_15(BookData bookData) {
        super(bookData);
    }

    @Override
    public void open(Player player) {
        ItemStack book = createItem();
        player.openBook(book);
    }
}
