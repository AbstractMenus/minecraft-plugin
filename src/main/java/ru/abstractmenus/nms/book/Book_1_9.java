package ru.abstractmenus.nms.book;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.data.BookData;
import ru.abstractmenus.api.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class Book_1_9 extends Book {

    private static MethodHandle getHandle;
    private static MethodHandle openBook;
    private static MethodHandle asNMSCopyMethod;
    private static Class<?> craftItemStack;
    private static Object enumMainHand;

    static {
        try {
            Class<?> itemStack = getNMSClass("ItemStack");
            Class<?> enumHand = getNMSClass("EnumHand");
            craftItemStack = getCraftBukkitClass("CraftItemStack", Package.INVENTORY);
            enumMainHand = getNMSClass("EnumHand").getEnumConstants()[0];
            getHandle = MethodHandles.lookup().unreflect(
                    getCraftBukkitClass("CraftPlayer", Package.ENTITY).getMethod("getHandle"));
            openBook = MethodHandles.lookup().unreflect(
                    getNMSClass("EntityPlayer").getMethod("a", itemStack, enumHand));
            asNMSCopyMethod = MethodHandles.lookup().unreflect(
                    craftItemStack.getMethod("asNMSCopy", ItemStack.class)
            );
        } catch (ReflectiveOperationException e) {
            Logger.severe("Error while initializing book: " + e.getMessage());
        }
    }

    Book_1_9(BookData bookData) {
        super(bookData);
    }

    @Override
    public void open(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        ItemStack stack = createItem();

        try {
            player.getInventory().setItemInMainHand(stack);
            sendPacket(stack, player);
        } catch (Throwable e) {
            Logger.severe("Error while opening book: " + e.getMessage());
        }

        player.getInventory().setItemInMainHand(held);
    }

    private void sendPacket(ItemStack i, Player p) throws Throwable {
        Object entityPlayer = getHandle.invoke(p);
        openBook.invoke(entityPlayer, getItemStack(i), enumMainHand);
    }

    private Object getItemStack(ItemStack item) throws Throwable {
        return asNMSCopyMethod.invoke(item);
    }
}
