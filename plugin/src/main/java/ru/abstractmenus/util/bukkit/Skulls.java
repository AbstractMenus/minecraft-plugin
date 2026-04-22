package ru.abstractmenus.util.bukkit;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import ru.abstractmenus.api.Logger;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Skulls {

    private static final Map<String, ItemStack> skullCache = new ConcurrentHashMap<>();
    /**
     * Cache of resolved player-skin skulls keyed by lowercased player name.
     * Populated lazily by {@link #getPlayerSkull(String)} and invalidated by
     * {@link #invalidatePlayerSkull(String)} (called from PlayerJoinEvent so a
     * rejoining player gets fresh skin data on the next query).
     */
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
        if (playerName == null) return null;

        String key = playerName.toLowerCase(Locale.ROOT);
        ItemStack cached = playerSkullCache.get(key);
        if (cached != null) return cached.clone();

        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            Logger.info("Player '" + playerName + "' is not online or not found.");
            return null;
        }

        PlayerProfile profile = player.getPlayerProfile();

        if (profile == null) {
            Logger.info("PlayerProfile for '" + playerName + "' not found.");
            return null;
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
