package ru.abstractmenus.data.actions;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.datatype.TypeBool;
import ru.abstractmenus.datatype.TypeFloat;
import ru.abstractmenus.datatype.TypeLocation;
import ru.abstractmenus.datatype.TypeSound;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.util.StringUtil;

@RequiredArgsConstructor
public class ActionSound implements Action {

    private final TypeSound sound;
    private final TypeFloat volume;
    private final TypeFloat pitch;
    private final TypeBool isPublic;
    private final TypeLocation location;

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if (sound != null) {
            Location loc = (location != null) ? location.getLocation(player, menu) : player.getLocation();
            Sound soundObj = sound.getSound(player, menu);
            if (soundObj == null) {
                return;
            }

            if (isPublic.getBool(player, menu)) {
                loc.getWorld().playSound(
                        loc,
                        soundObj,
                        volume.getFloat(player, menu),
                        pitch.getFloat(player, menu)
                );
            } else {
                player.playSound(
                        loc,
                        soundObj,
                        volume.getFloat(player, menu),
                        pitch.getFloat(player, menu)
                );
            }
        }
    }

    public static class Serializer implements NodeSerializer<ActionSound> {

        @Override
        public ActionSound deserialize(Class<ActionSound> type, ConfigNode node) throws NodeSerializeException {
            if (!node.isPrimitive() && !node.isMap()) {
                throw new NodeSerializeException(node, "Cannot read sound action. Invalid format");
            }

            String name = node.isPrimitive()
                    ? node.getString("null")
                    : node.node("name").getString(node.getString());

            if (!isValidSoundName(name)) {
                throw new NodeSerializeException(node, "Cannot read sound action with sound name '" + name + "'. Invalid sound name.");
            }

            TypeSound sound = new TypeSound(name);
            TypeFloat volume = getNodeValue(node, "volume", new TypeFloat(1.0f));
            TypeFloat pitch = getNodeValue(node, "pitch", new TypeFloat(1.0f));
            TypeBool isPublic = getNodeValue(node, "public", new TypeBool(false));
            TypeLocation location = getNodeValue(node, "location", null);

            return new ActionSound(sound, volume, pitch, isPublic, location);
        }

        private boolean isValidSoundName(String name) {
            NamespacedKey namespacedKey = NamespacedKey.fromString(name);
            if (namespacedKey == null) {
                return false;
            }

            return StringUtil.contains(name, '%') || Registry.SOUNDS.get(namespacedKey) != null;
        }

        @SuppressWarnings("unchecked")
        private <T> T getNodeValue(ConfigNode node, String key, T defaultValue) throws NodeSerializeException {
            ConfigNode childNode = node.node(key);
            return childNode.rawValue() != null ? childNode.getValue((Class<T>) defaultValue.getClass()) : defaultValue;
        }
    }

}