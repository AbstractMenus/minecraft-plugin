package ru.abstractmenus.datatype;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import javax.annotation.Nullable;

@Slf4j
public class TypeSound extends DataType {

    public TypeSound(String soundKey) {
        super(soundKey);
    }

    @Nullable
    public Sound getSound(Player player, Menu menu) {
        String resolvedKey = replaceFor(player, menu);
        NamespacedKey namespacedKey = NamespacedKey.fromString(resolvedKey);
        if (namespacedKey == null) {
            log.error("Invalid sound name: {}", resolvedKey);
            return null;
        }
        Sound sound = Registry.SOUNDS.get(namespacedKey);
        if (sound == null) {
            log.error("Could not found Sound '{}' from Registry", resolvedKey);
        }
        return sound;
    }

    public static class Serializer implements NodeSerializer<TypeSound> {

        @Override
        public TypeSound deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            String soundKey = node.getString();
            NamespacedKey namespacedKey = NamespacedKey.fromString(soundKey);
            if (namespacedKey == null) {
                throw new NodeSerializeException(node, "Invalid sound name: " + soundKey);
            }
            Sound sound = Registry.SOUNDS.get(namespacedKey);
            if (sound == null) {
                throw new NodeSerializeException(node, "Could not found Sound '" + soundKey + "' from Registry");
            }
            return new TypeSound(soundKey);
        }
    }
}
