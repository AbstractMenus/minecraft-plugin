package ru.abstractmenus.serializers;


import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.Locale;

public class BannerPatternSerializer implements NodeSerializer<Pattern> {

    @Override
    public Pattern deserialize(Class<Pattern> token, ConfigNode node) throws NodeSerializeException {
        String typeStr = node.node("type").getString("null").toUpperCase();
        String colorStr = node.node("color").getString("null").toUpperCase();

        PatternType type;
        DyeColor color;

        NamespacedKey key = NamespacedKey.fromString(typeStr.toLowerCase(Locale.ROOT));
        type = key == null ? null : RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN).get(key);
        if (type == null) {
            throw new NodeSerializeException(node.node("type"), "Cannot find banner pattern type: " + typeStr);
        }

        try {
            color = DyeColor.valueOf(colorStr);
        } catch (IllegalArgumentException e) {
            throw new NodeSerializeException(node.node("color"), "Cannot find pattern color: " + colorStr);
        }

        return new Pattern(color, type);
    }
}
