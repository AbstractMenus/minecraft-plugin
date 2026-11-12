package ru.abstractmenus.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.ArrayList;
import java.util.List;

public class BannerData {

    private final List<Pattern> patterns;

    public BannerData(List<Pattern> patterns){
        this.patterns = patterns;
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public static class Serializer implements NodeSerializer<BannerData> {

        private static final JsonParser JSON_PARSER = new JsonParser();

        @Override
        public BannerData deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if (node.isList() || node.isMap()) {
                return new BannerData(node.getList(Pattern.class));
            } else {
                return new BannerData(parseFromJson(node, node.getString()));
            }
        }

        private List<Pattern> parseFromJson(ConfigNode node, String json) throws NodeSerializeException {
            List<Pattern> patterns = new ArrayList<>();
            JsonObject root = JSON_PARSER.parse(json).getAsJsonObject();
            JsonArray patternsArray = root.get("BlockEntityTag").getAsJsonObject()
                    .get("Patterns").getAsJsonArray();

            for (JsonElement element : patternsArray){
                JsonObject object = element.getAsJsonObject();
                String pattern = object.get("Pattern").getAsString();
                byte colorByte = object.get("Color").getAsByte();

                // Banner JSON uses compact pattern identifiers.
                String patternKey = switch (pattern) {
                    case "b" -> "base";
                    case "bl" -> "square_bottom_left";
                    case "br" -> "square_bottom_right";
                    case "tl" -> "square_top_left";
                    case "tr" -> "square_top_right";
                    case "bs" -> "stripe_bottom";
                    case "ts" -> "stripe_top";
                    case "ls" -> "stripe_left";
                    case "rs" -> "stripe_right";
                    case "cs" -> "stripe_center";
                    case "ms" -> "stripe_middle";
                    case "drs" -> "stripe_downright";
                    case "dls" -> "stripe_downleft";
                    case "ss" -> "small_stripes";
                    case "cr" -> "cross";
                    case "sc" -> "straight_cross";
                    case "bt" -> "triangle_bottom";
                    case "tt" -> "triangle_top";
                    case "bts" -> "triangles_bottom";
                    case "tts" -> "triangles_top";
                    case "ld" -> "diagonal_left";
                    case "rd" -> "diagonal_up_right";
                    case "lud" -> "diagonal_up_left";
                    case "rud" -> "diagonal_right";
                    case "mc" -> "circle";
                    case "mr" -> "rhombus";
                    case "vh" -> "half_vertical";
                    case "hh" -> "half_horizontal";
                    case "vhr" -> "half_vertical_right";
                    case "hhb" -> "half_horizontal_bottom";
                    case "bo" -> "border";
                    case "cbo" -> "curly_border";
                    case "cre" -> "creeper";
                    case "gra" -> "gradient";
                    case "gru" -> "gradient_up";
                    case "bri" -> "bricks";
                    case "sku" -> "skull";
                    case "flo" -> "flower";
                    case "moj" -> "mojang";
                    case "glb" -> "globe";
                    case "pig" -> "piglin";
                    case "flw" -> "flow";
                    case "gus" -> "guster";
                    default -> pattern;
                };
                NamespacedKey key = NamespacedKey.fromString(patternKey);
                PatternType patternType = key == null ? null : RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN).get(key);
                DyeColor color = DyeColor.getByWoolData(colorByte);

                if (patternType == null) throw new NodeSerializeException(node, "Cannot serialize banner pattern from JSON");
                if (color == null) throw new NodeSerializeException(node, "Cannot serialize banner color from JSON");

                patterns.add(new Pattern(color, patternType));
            }

            return patterns;
        }
    }
}
