package ru.abstractmenus.handlers;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinApplier;
import net.skinsrestorer.api.property.SkinIdentifier;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.abstractmenus.api.handler.SkinHandler;
import ru.abstractmenus.services.proxy.dto.ChangeSkinDTO;

import static org.bukkit.Bukkit.getServer;

public class SkinsRestorerHandler implements SkinHandler {

    private SkinsRestorer api;
    private SkinApplier<Player> applier;
    private SkinStorage skinStorage;
    private PlayerStorage playerStorage;
    private final boolean isProxyMode;
    private final Gson gson = new GsonBuilder().create();
    private final Plugin plugin;

    public SkinsRestorerHandler(boolean isProxyMode, Plugin plugin) {
        this.isProxyMode = isProxyMode;
        this.plugin = plugin;
        try {
            this.api = SkinsRestorerProvider.get();
            this.applier = api.getSkinApplier(Player.class);
            this.skinStorage = api.getSkinStorage();
            this.playerStorage = api.getPlayerStorage();
        } catch (Throwable th) {
            // Ignore
        }
    }

    @Override
    public void setSkin(Player player, String texture, String signature) {
        if (api == null && !isProxyMode) {
            return;
        }
        if (isProxyMode) {
            setProxySkin(player, texture, signature);
        } else {
            setServerSkin(player, texture, signature);
        }
    }

    private void setProxySkin(Player player, String texture, String signature) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        ChangeSkinDTO changeSkinDTO = new ChangeSkinDTO()
                .setTexture(texture)
                .setSignature(signature)
                .setPlayerName(player.getName())
                .setPlayerUUID(player.getUniqueId());

        String json = gson.toJson(changeSkinDTO);
        out.writeUTF("ChangeSkin");
        out.writeUTF(json);

        getServer().sendPluginMessage(plugin, "abstractmenus:main", out.toByteArray());
    }

    private void setServerSkin(Player player, String texture, String signature) {

        SkinIdentifier identifier = createIdentifier(player);
        SkinProperty property = SkinProperty.of(texture, signature);

        skinStorage.setPlayerSkinData(
                player.getUniqueId(),
                player.getName(),
                property,
                System.currentTimeMillis()
        );

        playerStorage.setSkinIdOfPlayer(player.getUniqueId(), identifier);

        try {
            applier.applySkin(player);
        } catch (Exception e) {
            // Ignore
        }
    }

    @Override
    public void resetSkin(Player player) {
        if (api == null && !isProxyMode) {
            return;
        }
        if (isProxyMode) {
            resetProxySkin(player);
        } else {
            resetServerSkin(player);
        }
    }

    private void resetProxySkin(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("ResetSkin");
        out.writeUTF(player.getUniqueId().toString());

        getServer().sendPluginMessage(plugin, "abstractmenus:main", out.toByteArray());
    }

    private void resetServerSkin(Player player) {
        SkinIdentifier identifier = createIdentifier(player);

        api.getSkinStorage().removeSkinData(identifier);

        try {
            applier.applySkin(player);
        } catch (Exception e) {
            // Ignore
        }
    }

    private SkinIdentifier createIdentifier(Player player) {
        return SkinIdentifier.ofPlayer(player.getUniqueId());
    }
}
