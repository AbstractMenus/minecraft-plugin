package ru.abstractmenus.nms.book;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.data.BookData;
import ru.abstractmenus.api.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class Book_1_8 extends Book {

    private static MethodHandle getHandle;
    private static MethodHandle openBook;
    private static MethodHandle asNMSCopyMethod;
    private static Class<?> craftItemStack;

    static {
        try {
            Class<?> itemStack = getNMSClass("ItemStack");
            craftItemStack = getCraftBukkitClass("CraftItemStack", Package.INVENTORY);
            getHandle = MethodHandles.lookup().unreflect(getCraftBukkitClass("CraftPlayer", Package.ENTITY).getMethod("getHandle"));
            openBook = MethodHandles.lookup().unreflect(getNMSClass("EntityPlayer").getMethod("openBook", itemStack));

            asNMSCopyMethod = MethodHandles.lookup().unreflect(
                    craftItemStack.getMethod("asNMSCopy", ItemStack.class)
            );
        } catch (ReflectiveOperationException e) {
            Logger.severe("Error while initializing book: " + e.getMessage());
        }
    }

    Book_1_8(BookData bookData) {
        super(bookData);
    }

    @Override
    public void open(Player player) {
        ItemStack held = player.getInventory().getItemInHand();
        ItemStack stack = createItem();

        try {
            player.getInventory().setItemInHand(stack);
            sendPacket(stack, player);
        } catch (Throwable e) {
            Logger.severe("Error while opening book: " + e.getMessage());
        }

        player.getInventory().setItemInHand(held);
    }

    private void sendPacket(ItemStack i, Player p) throws Throwable {
        Object entityPlayer = getHandle.invoke(p);
        openBook.invoke(entityPlayer, getItemStack(i));
    }

    private Object getItemStack(ItemStack item) {
        try {
            return asNMSCopyMethod.invoke(item);
        } catch (Throwable e) {
            Logger.severe("Error while invoke method asNMSCopy in CraftItemStack");
        }
        return null;
    }
}
