package ru.abstractmenus.nms.book;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import ru.abstractmenus.data.BookData;
import ru.abstractmenus.util.NMS;

public abstract class Book extends NMS {

    private final BookData bookData;

    protected Book(BookData bookData){
        this.bookData = bookData;
    }

    public abstract void open(Player player);

    ItemStack createItem(){
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();

        if(meta != null){
            meta.setAuthor(bookData.getAuthor());
            meta.setTitle(bookData.getTitle());
            meta.setPages(bookData.getPages());
            item.setItemMeta(meta);
        }

        return item;
    }

    public static Book create(BookData bookData){
        switch (getVersion()){
            case "v1_8_R1":
            case "v1_8_R2":
            case "v1_8_R3":
            case "v1_8_R4":
                return new Book_1_8(bookData);
            case "v1_9_R1":
            case "v1_9_R2":
            case "v1_9_R3":
            case "v1_9_R4":
            case "v1_10_R1":
            case "v1_10_R2":
            case "v1_11_R1":
            case "v1_11_R2":
            case "v1_12_R1":
            case "v1_12_R2":
                return new Book_1_9(bookData);
            case "v1_13_R1":
            case "v1_13_R2":
                return new Book_1_13(bookData);
            case "v1_14_R1":
            case "v1_14_R2":
            case "v1_14_R3":
            case "v1_14_R4":
                return new Book_1_14(bookData);
            default:
                return new Book_1_15(bookData);
        }
    }

}
