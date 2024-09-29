package ru.abstractmenus.services;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.util.bukkit.MojangApi;
import ru.abstractmenus.util.bukkit.BukkitTasks;
import ru.abstractmenus.util.StringUtil;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ProfileStorage implements Listener {

    private static ProfileStorage instance;

    public static final GameProfile DEF_PROFILE = new GameProfile(UUID.randomUUID(), StringUtil.generateRandom(16));

    private final Map<String, GameProfile> profiles = new HashMap<>();

    public ProfileStorage() {
        instance = this;
    }

    /**
     * Get URL to skin texture
     * @param playerName Player name
     * @return Found texture or null
     */
    public GameProfile getProfile(String playerName) {
        return profiles.get(playerName.toLowerCase());
    }

    public void add(String playerName, GameProfile profile) {
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
            GameProfile profile = fetchProfile(event.getPlayer());

            add(event.getPlayer().getName(), profile);

            if (profile != null) {
                Collection<Property> texture = profile.getProperties().get("textures");

                if (texture == null || texture.isEmpty())
                    profile = MojangApi.loadProfileWithSkin(event.getPlayer().getName());

                if (profile != null) {
                    add(event.getPlayer().getName(), profile);
                }
            }
        });
    }

    private GameProfile fetchProfile(Player player) {
        try {
            Method method = player.getClass().getMethod("getProfile");
            return (GameProfile) method.invoke(player);
        } catch (Throwable t) {
            Logger.warning("Cannot fetch game profile: " + t.getMessage());
            return null;
        }
    }

}
