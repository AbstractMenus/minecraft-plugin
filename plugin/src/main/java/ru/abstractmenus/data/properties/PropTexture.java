package ru.abstractmenus.data.properties;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.inventory.Menu;

import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.util.bukkit.ItemUtil;
import ru.abstractmenus.util.bukkit.Skulls;
import ru.abstractmenus.api.Handlers;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PropTexture implements ItemProperty {

    private static final String TEXTURE_PREFIX = "http://textures.minecraft.net/texture/";
    private static final String BASE64_PREFIX = "base64:";

    private final String texture;

    private PropTexture(String texture) {
        this.texture = texture;
    }

    @Override
    public boolean canReplaceMaterial() {
        return true;
    }

    @Override
    public boolean isApplyMeta() {
        return false;
    }

    @Override
    public void apply(ItemStack item, ItemMeta meta, Player player, Menu menu) {
        String replaced = Handlers.getPlaceholderHandler().replace(player, texture);
        ItemStack skullItem = Skulls.getCustomSkull(fetchTextureUrl(replaced));

        if (skullItem != null) {
            ItemUtil.merge(item, skullItem);
            return;
        }

        Logger.severe("Returned ItemStack is null. Failing generating head with texture");
    }

    private String fetchTextureUrl(String value) {
        if (isBase64(value)) {
            return value.substring(BASE64_PREFIX.length());
        } else if (isLink(value)) {
            return getBase64FromUrl(value);
        } else {
            return getBase64FromUrl(TEXTURE_PREFIX + value);
        }
    }

    private boolean isLink(String str) {
        return str.startsWith("http://") || str.startsWith("https://");
    }

    private boolean isBase64(String str) {
        return str.startsWith(BASE64_PREFIX);
    }

    public static String getBase64FromUrl(String textureUrl) {
        JsonObject textureData = new JsonObject();
        JsonObject textures = new JsonObject();
        JsonObject skin = new JsonObject();

        skin.addProperty("url", textureUrl);
        textures.add("SKIN", skin);
        textureData.add("textures", textures);

        String json = textureData.toString();
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Pre-build and cache skull for static textures (no placeholders).
     */
    public void warmup() {
        if (texture.indexOf('%') != -1) return;
        Skulls.getCustomSkull(fetchTextureUrl(texture));
    }

    public static class Serializer implements NodeSerializer<PropTexture> {

        @Override
        public PropTexture deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropTexture(node.getString());
        }
    }
}
