package ru.abstractmenus.nms.book;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.data.BookData;

import java.lang.reflect.Constructor;

public class Book_1_13 extends Book {

    Book_1_13(BookData bookData) {
        super(bookData);
    }

    @Override
    public void open(Player player) {
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack book = createItem();
        ItemStack old = player.getInventory().getItem(slot);
        player.getInventory().setItem(slot, book);

        ByteBuf buf = Unpooled.buffer(256);
        buf.setByte(0, (byte)0);
        buf.writerIndex(1);

        try {
            Constructor<?> serializerConstructor = getNMSClass("PacketDataSerializer").getConstructor(ByteBuf.class);
            Object packetDataSerializer = serializerConstructor.newInstance(buf);

            Constructor<?> keyConstructor = getNMSClass("MinecraftKey").getConstructor(String.class);
            Object bookKey = keyConstructor.newInstance("minecraft:book_open");

            Constructor<?> titleConstructor = getNMSClass("PacketPlayOutCustomPayload").getConstructor(bookKey.getClass(), getNMSClass("PacketDataSerializer"));
            Object payload = titleConstructor.newInstance(bookKey, packetDataSerializer);

            sendPacket(player, payload);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            player.getInventory().setItem(slot, old);
        }
    }
}
