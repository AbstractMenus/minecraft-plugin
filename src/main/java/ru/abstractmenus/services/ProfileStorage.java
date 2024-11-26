package ru.abstractmenus.services;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.profile.PlayerTextures;
import ru.abstractmenus.util.StringUtil;
import ru.abstractmenus.util.bukkit.BukkitTasks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ProfileStorage implements Listener {

    private static ProfileStorage instance;

    public static final PlayerProfile DEF_PROFILE = Bukkit.createProfile(UUID.randomUUID(), StringUtil.generateRandom(16));

    private final Map<String, PlayerProfile> profiles = new HashMap<>();

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
        BukkitTasks.runTaskAsync(() -> {
            Player player = event.getPlayer();
            PlayerProfile profile = player.getPlayerProfile();

            try {
                if (!profile.isComplete()) {
                    profile.complete(true);
                }

                PlayerTextures textures = profile.getTextures();
                if (textures.getSkin() == null) {
                    add(player.getName(), DEF_PROFILE);
                } else {
                    add(player.getName(), profile);
                }
            } catch (Exception e) {
                add(player.getName(), DEF_PROFILE);
            }
        });
    }
}
