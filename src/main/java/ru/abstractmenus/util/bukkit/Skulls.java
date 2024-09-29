package ru.abstractmenus.util.bukkit;

import com.mojang.authlib.GameProfile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.services.ProfileStorage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Skulls {

    private Skulls() { }

    public static ItemStack getCustomSkull(String url) {
        GameProfile profile = MojangApi.createProfile(url);
        return getCustomSkull(profile);
    }

    public static ItemStack getCustomSkull(GameProfile profile) {
        ItemStack head = createSkullItem();

        if (profile == null) return head;

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();

        if (headMeta == null) return null;

        Field profileField;

        try {
            Method method = headMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            method.setAccessible(true);
            method.invoke(headMeta, profile);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            try {
                profileField = headMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(headMeta, profile);
            } catch (NoSuchFieldException | IllegalAccessException ex2) {
                ex2.printStackTrace();
            }
        }

        head.setItemMeta(headMeta);

        return head;
    }

    public static ItemStack getPlayerSkull(String playerName) {
        GameProfile profile = ProfileStorage.instance().getProfile(playerName);

        if (profile == null) {
            Logger.info("Profile '" + playerName + "' not found. Trying to load ...");

            profile = MojangApi.loadProfileWithSkin(playerName);

            if (profile == null)
                profile = ProfileStorage.DEF_PROFILE;

            ProfileStorage.instance().add(playerName, profile);
        }

        return getCustomSkull(profile);
    }

    public static ItemStack createSkullItem() {
        try {
            return new ItemStack(ItemUtil.getHeadMaterial(), 1, (short) 3);
        } catch (Throwable t) {
            return new ItemStack(ItemUtil.getHeadMaterial());
        }
    }

}
