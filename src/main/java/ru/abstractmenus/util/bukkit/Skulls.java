package ru.abstractmenus.util.bukkit;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import ru.abstractmenus.services.ProfileStorage;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public final class Skulls {

    private Skulls() {
    }

    public static ItemStack getCustomSkull(String url) {
        ItemStack head = createSkullItem();
        if (url == null || url.isEmpty()) {
            return head;
        }

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        if (headMeta == null) {
            return null;
        }

        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());

        try {
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URI(url).toURL());
            profile.setTextures(textures);

            headMeta.setPlayerProfile(profile);
            head.setItemMeta(headMeta);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(String.format("Bad URL [%s] for texture [%s]", url, profile.getTextures()), e);
        }

        return head;
    }

    public static ItemStack getCustomSkull(PlayerProfile profile) {
        ItemStack head = createSkullItem();
        if (profile == null) return head;

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        if (headMeta == null) return null;

        headMeta.setPlayerProfile(profile);
        head.setItemMeta(headMeta);

        return head;
    }

    public static ItemStack getPlayerSkull(String playerName) {
        PlayerProfile profile = ProfileStorage.instance().getProfile(playerName);

        if (profile == null) {
            profile = Bukkit.createProfile(null, playerName);
            try {
                profile.complete(true);
            } catch (Exception e) {
                profile = ProfileStorage.DEF_PROFILE;
            }
            ProfileStorage.instance().add(playerName, profile);
        }

        return getCustomSkull(profile);
    }

    public static ItemStack createSkullItem() {
        return new ItemStack(ItemUtil.getHeadMaterial());
    }
}
