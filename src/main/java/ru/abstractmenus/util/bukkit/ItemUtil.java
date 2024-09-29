package ru.abstractmenus.util.bukkit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import ru.abstractmenus.util.NMS;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Base64;

public final class ItemUtil {

    private static MethodHandle getByIdMethod;
    private static final ItemStack EMPTY_ITEM = new ItemStack(Material.AIR, 0);

    private ItemUtil() { }

    public static ItemStack empty() {
        return EMPTY_ITEM;
    }

    public static Material getHeadMaterial() {
        Material mat = Material.getMaterial("PLAYER_HEAD");
        return mat != null ? mat : Material.getMaterial("SKULL_ITEM");
    }

    public static <T> Material get(T value) {
        if (value instanceof Material) return (Material) value;
        if (value instanceof Integer) return getById((Integer) value);
        return Material.getMaterial(value.toString());
    }

    public static Material getById(int id) {
        try {
            if (getByIdMethod != null)
                return (Material) getByIdMethod.invoke(id);
        } catch (Throwable ignore) { }

        throw new IllegalArgumentException("Could not load material by id. You have to use material names");
    }

    public static void merge(ItemStack itemTo, ItemStack itemFrom) {
        itemTo.setType(itemFrom.getType());

        if (NMS.getMinorVersion() <= 12)
            itemTo.setData(itemFrom.getData());

        try {
            itemTo.setDurability(itemFrom.getDurability());
        } catch (Throwable ignore) { }

        itemTo.setAmount(itemFrom.getAmount());
        itemTo.setItemMeta(itemFrom.getItemMeta());
    }

    @Nullable
    public static String encodeStack(ItemStack item) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream stream = new BukkitObjectOutputStream(out);
            stream.writeObject(item);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static ItemStack decodeStack(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream stream = new BukkitObjectInputStream(in);
            return (ItemStack) stream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isSimilar(ItemStack source, ItemStack compared) {
        if (source == compared) return true;
        if (source == null || compared == null) return false;
        if (source.getType() == Material.AIR && compared.getType() == Material.AIR) return true;
        return source.isSimilar(compared) && compared.getAmount() >= source.getAmount();
    }

    static {
        try {
            Method method = Material.class.getMethod("getMaterial", int.class);
            getByIdMethod = MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            // Ignore
        }
    }
}
