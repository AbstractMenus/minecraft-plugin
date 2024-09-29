package ru.abstractmenus.util.bukkit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.util.NMS;
import ru.abstractmenus.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;
import java.util.UUID;

public final class MojangApi {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
    private static final JsonParser JSON_PARSER = new JsonParser();

    private MojangApi() { }

    public static UUID getUUID(String name) {
        try (InputStream in = new URL(String.format(UUID_URL, name)).openStream()) {
            String jsonString = getLine(in);

            if(jsonString != null && !jsonString.isEmpty()) {
                JsonObject object = JSON_PARSER.parse(jsonString).getAsJsonObject();
                String uuid = object.get("id").getAsString();
                return UuidUtil.getUUID(uuid);
            }
        } catch (IOException ignore) { }
        return null;
    }

    public static String getTexture(UUID premiumUuid) {
        String url = String.format(SKIN_URL, clearUUID(premiumUuid));

        try (InputStream in = new URL(url).openStream()) {
            String jsonString = getLine(in);

            if (jsonString != null && !jsonString.isEmpty()) {
                JsonObject object = JSON_PARSER.parse(jsonString).getAsJsonObject();

                if (object.has("properties")) {
                    JsonArray propArr = object.get("properties").getAsJsonArray();

                    if (propArr.size() > 0) {
                        JsonObject properties = object.get("properties").getAsJsonArray().get(0).getAsJsonObject();

                        return properties.get("value").getAsString();
                    }
                }
            }
        } catch (Exception e){
            Logger.warning(String.format("Cannot fetch premium skin of %s: %s", premiumUuid, e.getMessage()));
        }

        return null;
    }

    public static String getSkinUrl(UUID premiumUuid) {
        return textureToUrl(getTexture(premiumUuid));
    }

    public static GameProfile createProfile(String skinUrl) {
        GameProfile profile;

        if (NMS.getMinorVersion() >= 20) {
            profile = new GameProfile(generateUUID(skinUrl), "");
        } else {
            profile = new GameProfile(generateUUID(skinUrl), null);
        }

        if (profile.getProperties() == null)
            throw new IllegalStateException("Profile doesn't contains a property map");

        byte[] prop = String.format("{textures:{SKIN:{url:\"%s\"}}}", skinUrl).getBytes(StandardCharsets.UTF_8);
        String encodedData = Base64.getEncoder().encodeToString(prop);
        profile.getProperties().put("textures", new Property("textures", encodedData));

        return profile;
    }

    public static GameProfile loadProfileWithSkin(String playerName) {
        UUID uuid = MojangApi.getUUID(playerName);
        GameProfile profile = null;

        if (uuid != null) {
            String url = MojangApi.getSkinUrl(uuid);

            if (url != null)
                profile = MojangApi.createProfile(url);
        }

        return profile;
    }

    private static String textureToUrl(String base64String) throws JsonParseException {
        if (base64String == null) return null;

        String encoded = new String(BASE64_DECODER.decode(base64String));
        JsonObject json = JSON_PARSER.parse(encoded).getAsJsonObject();
        JsonObject textures = json.get("textures").getAsJsonObject();

        if(textures.entrySet().size() != 0){
            return textures.get("SKIN").getAsJsonObject().get("url").getAsString();
        }

        return null;
    }

    private static String getLine(InputStream in){
        if (in == null) return null;

        Scanner scanner = new Scanner(in);
        StringBuilder builder = new StringBuilder();
        while(scanner.hasNext()) builder.append(scanner.next());
        return builder.toString();
    }

    private static String clearUUID(UUID uuid){
        return uuid.toString().replace("-", "");
    }

    private static UUID generateUUID(String url){
        return UUID.nameUUIDFromBytes(("AbstractMenusProfile:" + url).getBytes(StandardCharsets.UTF_8));
    }
}
