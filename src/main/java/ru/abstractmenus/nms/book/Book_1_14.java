package ru.abstractmenus.nms.book;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.data.BookData;
import ru.abstractmenus.api.Logger;

import java.lang.reflect.Constructor;

public class Book_1_14 extends Book {

    Book_1_14(BookData bookData) {
        super(bookData);
    }

    @Override
    public void open(Player player) {
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack book = createItem();
        ItemStack old = player.getInventory().getItem(slot);

        player.getInventory().setItem(slot, book);

        try {
            Object hand = getNMSClass("EnumHand").getField("MAIN_HAND").get(null);
            Constructor<?> constructor = getNMSClass("PacketPlayOutOpenBook")
                    .getConstructor(getNMSClass("EnumHand"));
            Object packet = constructor.newInstance(hand);
            sendPacket(player, packet);
        } catch (ReflectiveOperationException e) {
            Logger.severe("Error while open book: " + e.getMessage());
        } finally {
            player.getInventory().setItem(slot, old);
        }
    }
}
