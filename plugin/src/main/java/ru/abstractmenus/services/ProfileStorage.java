package ru.abstractmenus.services;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.util.bukkit.MojangApi;
import ru.abstractmenus.util.bukkit.BukkitTasks;
import ru.abstractmenus.util.StringUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProfileStorage implements Listener {

    private static ProfileStorage instance;

    public static final PlayerProfile DEF_PROFILE = Bukkit.createProfile(UUID.randomUUID(), StringUtil.generateRandom(16));

    private final Map<String, PlayerProfile> profiles = new ConcurrentHashMap<>();

    public ProfileStorage() {
        instance = this;
    }

    /**
     * Get URL to skin texture
     *
     * @param playerName Player name
     * @return Found texture or null
     */
    public PlayerProfile getProfile(String playerName) {
        return profiles.get(playerName.toLowerCase());
    }

    public void add(String playerName, PlayerProfile profile) {
        profiles.put(playerName.toLowerCase(), profile);
    }

    public void remove(Player player) {
        profiles.remove(player.getName().toLowerCase());
    }

    public static ProfileStorage instance() {
        return instance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Capture profile on main thread (Bukkit API requirement)
        PlayerProfile profile = fetchProfile(event.getPlayer());
        String playerName = event.getPlayer().getName();

        add(playerName, profile);

        // HTTP calls to Mojang API run async
        BukkitTasks.runTaskAsync(() -> {
            boolean hasTexture = profile != null && profile.getProperties().stream()
                    .anyMatch(p -> p.getName().equals("textures"));

            if (!hasTexture) {
                PlayerProfile fetched = MojangApi.loadProfileWithSkin(playerName);
                if (fetched != null) {
                    add(playerName, fetched);
                }
            }
        });
    }

    private PlayerProfile fetchProfile(Player player) {
        try {
            return player.getPlayerProfile();
        } catch (Throwable t) {
            Logger.warning("Cannot fetch game profile: " + t.getMessage());
            return null;
        }
    }

}
