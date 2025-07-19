package ru.abstractmenus.util.bukkit;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.services.ProfileStorage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public final class Skulls {

    private Skulls() {}

    public static ItemStack getCustomSkull(String url) {
        GameProfile profile = MojangApi.createProfile(url);
        return getCustomSkull(profile);
    }

    public static ItemStack getCustomSkull(GameProfile profile) {
        ItemStack head = createSkullItem();
        if (profile == null) return head;

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        if (headMeta == null) return head;

        if (applyModernProfile(headMeta, profile)) {
            head.setItemMeta(headMeta);
            return head;
        }

        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}

        head.setItemMeta(headMeta);
        return head;
    }

    private static boolean applyModernProfile(SkullMeta headMeta, GameProfile profile) {
        try {
            Class<?> playerProfileClass = Class.forName("org.bukkit.profile.PlayerProfile");
            Method createProfile = Bukkit.class.getMethod("createProfile", UUID.class, String.class);
            Object playerProfile = createProfile.invoke(null, profile.getId(), profile.getName());

            Method getProperties = playerProfileClass.getMethod("getProperties");
            Object properties = getProperties.invoke(playerProfile);

            Class<?> propertyMapClass = properties.getClass();
            Method put = propertyMapClass.getMethod("put", Object.class, Object.class);

            for (Map.Entry<String, Property> entry : profile.getProperties().entries()) {
                put.invoke(properties, entry.getKey(), entry.getValue());
            }

            Method setPlayerProfile = headMeta.getClass().getMethod("setPlayerProfile", playerProfileClass);
            setPlayerProfile.invoke(headMeta, playerProfile);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
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
