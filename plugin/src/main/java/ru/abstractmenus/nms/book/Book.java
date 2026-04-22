package ru.abstractmenus.nms.book;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import ru.abstractmenus.data.BookData;

public abstract class Book {

    private final BookData bookData;

    protected Book(BookData bookData) {
        this.bookData = bookData;
    }

    public abstract void open(Player player);

    ItemStack createItem() {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();

        if (meta != null) {
            meta.setAuthor(bookData.getAuthor());
            meta.setTitle(bookData.getTitle());
            meta.setPages(bookData.getPages());
            item.setItemMeta(meta);
        }

        return item;
    }

    public static Book create(BookData bookData) {
        return new Book_1_15(bookData);
    }

}
