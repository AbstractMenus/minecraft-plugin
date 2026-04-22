package ru.abstractmenus.util.bukkit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Base64;

public final class ItemUtil {

    private static final ItemStack EMPTY_ITEM = new ItemStack(Material.AIR, 0);

    private ItemUtil() {
    }

    public static ItemStack empty() {
        return EMPTY_ITEM;
    }

    public static Material getHeadMaterial() {
        Material mat = Material.getMaterial("PLAYER_HEAD");
        return mat != null ? mat : Material.getMaterial("SKULL_ITEM");
    }

    public static <T> Material get(T value) {
        if (value instanceof Material) return (Material) value;
        return Material.getMaterial(value.toString());
    }

    public static void merge(ItemStack itemTo, ItemStack itemFrom) {
        itemTo.setType(itemFrom.getType());
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

}
