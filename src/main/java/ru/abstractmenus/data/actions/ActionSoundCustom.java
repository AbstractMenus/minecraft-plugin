package ru.abstractmenus.data.actions;

import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.datatype.TypeBool;
import ru.abstractmenus.datatype.TypeEnum;
import ru.abstractmenus.datatype.TypeFloat;
import ru.abstractmenus.datatype.TypeLocation;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.util.StringUtil;

public class ActionSoundCustom implements Action {

    private final String sound;
    private final TypeEnum<SoundCategory> category;
    private final TypeFloat volume, pitch;
    private final TypeBool isPublic;
    private final TypeLocation location;

    private ActionSoundCustom(
            String sound,
            TypeEnum<SoundCategory> category,
            TypeFloat volume,
            TypeFloat pitch,
            TypeBool isPublic,
            TypeLocation location
    ) {
        this.sound = sound;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
        this.isPublic = isPublic;
        this.location = location;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if(sound != null) {
            Location loc = (location != null) ? location.getLocation(player, menu) : player.getLocation();

            if(isPublic.getBool(player, menu) && loc.getWorld() != null) {
                loc.getWorld().playSound(loc,
                        sound,
                        category.getEnum(SoundCategory.class, player, menu),
                        volume.getFloat(player, menu),
                        pitch.getFloat(player, menu));
                return;
            }

            player.playSound(loc,
                    sound,
                    category.getEnum(SoundCategory.class, player, menu),
                    volume.getFloat(player, menu),
                    pitch.getFloat(player, menu));
        }
    }

    public static class Serializer implements NodeSerializer<ActionSoundCustom> {

        @Override
        public ActionSoundCustom deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if (node.isPrimitive()) {
                return new ActionSoundCustom(
                        node.getString("null"),
                        new TypeEnum<>(SoundCategory.MASTER),
                        new TypeFloat(1.0f),
                        new TypeFloat(1.0f),
                        new TypeBool(false),
                        null
                );
            }

            if (node.isMap()) {
                String sound = node.node("name").getString();
                TypeEnum<SoundCategory> category = new TypeEnum<>(SoundCategory.MASTER);
                TypeFloat volume = new TypeFloat(1.0f);
                TypeFloat pitch = new TypeFloat(1.0f);
                TypeBool isPublic = new TypeBool(false);
                TypeLocation location = null;

                if (node.node("category").rawValue() != null) {
                    String name = node.node("name").getString(node.getString());

                    try {
                        category = new TypeEnum<>(SoundCategory.valueOf(name));
                    } catch (IllegalArgumentException e1){
                        if (!StringUtil.contains(name, '%'))
                            throw new NodeSerializeException(node, "Cannot read sound category with sound name '"+name+"'. Invalid category name.");
                        category = new TypeEnum<>(name);
                    }
                }

                if(node.node("volume").rawValue() != null){
                    volume = node.node("volume").getValue(TypeFloat.class);
                }

                if(node.node("pitch").rawValue() != null){
                    pitch = node.node("pitch").getValue(TypeFloat.class);
                }

                if(node.node("public").rawValue() != null){
                    isPublic = node.node("public").getValue(TypeBool.class);
                }

                if(node.node("location").rawValue() != null){
                    location = node.node("location").getValue(TypeLocation.class);
                }

                return new ActionSoundCustom(sound, category, volume, pitch, isPublic, location);
            }

            throw new NodeSerializeException(node, "Cannot read sound action. Invalid format");
        }

    }
}
