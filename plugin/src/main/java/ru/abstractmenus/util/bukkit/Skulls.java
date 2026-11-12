package ru.abstractmenus.util.bukkit;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class Skulls {

    private static final Pattern PLAYER_NAME = Pattern.compile("[!-~]{1,16}");

    private static final Map<String, ItemStack> skullCache = new ConcurrentHashMap<>();
    // PlayerJoinEvent invalidates cached heads so online skin changes are picked up.
    private static final Map<String, ItemStack> playerSkullCache = new ConcurrentHashMap<>();

    private Skulls() {
    }

    public static void clearCache() {
        skullCache.clear();
        playerSkullCache.clear();
    }

    public static void invalidatePlayerSkull(String playerName) {
        if (playerName != null) {
            playerSkullCache.remove(playerName.toLowerCase(Locale.ROOT));
        }
    }

    public static ItemStack getCustomSkull(String texture) {
        ItemStack cached = skullCache.get(texture);
        if (cached != null) return cached.clone();

        UUID uuid = UUID.nameUUIDFromBytes(("Skull:" + texture).getBytes());

        PlayerProfile profile = Bukkit.createProfile(uuid);
        profile.setProperty(new ProfileProperty("textures", texture));

        ItemStack result = getCustomSkull(profile);
        if (result != null) {
            skullCache.put(texture, result);
            return result.clone();
        }
        return result;
    }

    public static ItemStack getCustomSkull(com.destroystokyo.paper.profile.PlayerProfile profile) {
        ItemStack head = createSkullItem();

        if (profile == null) return head;

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();

        if (headMeta == null) return null;

        headMeta.setPlayerProfile(profile);
        head.setItemMeta(headMeta);

        return head;
    }

    public static ItemStack getPlayerSkull(String playerName) {
        if (playerName == null || !PLAYER_NAME.matcher(playerName).matches()) return null;

        String key = playerName.toLowerCase(Locale.ROOT);
        ItemStack cached = playerSkullCache.get(key);
        if (cached != null) return cached.clone();

        Player player = Bukkit.getPlayerExact(playerName);
        PlayerProfile profile = player != null ? player.getPlayerProfile() : null;
        if (profile == null || !profile.hasTextures()) {
            profile = Bukkit.createProfileExact(null, playerName);
        }

        ItemStack result = getCustomSkull(profile);
        if (result != null) {
            playerSkullCache.put(key, result);
            return result.clone();
        }
        return result;
    }

    public static ItemStack createSkullItem() {
        return new ItemStack(ItemUtil.getHeadMaterial());
    }
}
