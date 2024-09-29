package ru.abstractmenus.data.actions;

import ru.abstractmenus.datatype.TypeBool;
import ru.abstractmenus.datatype.TypeEnum;
import ru.abstractmenus.datatype.TypeFloat;
import ru.abstractmenus.datatype.TypeLocation;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.util.StringUtil;

public class ActionSound implements Action {

    private final TypeEnum<Sound> sound;
    private final TypeFloat volume, pitch;
    private final TypeBool isPublic;
    private final TypeLocation location;

    private ActionSound(TypeEnum<Sound> sound, TypeFloat volume, TypeFloat pitch, TypeBool isPublic, TypeLocation location){
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.isPublic = isPublic;
        this.location = location;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if(sound != null){
            Location loc = (location != null) ? location.getLocation(player, menu) : player.getLocation();

            if(isPublic.getBool(player, menu) && loc.getWorld() != null){
                loc.getWorld().playSound(loc,
                        sound.getEnum(Sound.class, player, menu),
                        volume.getFloat(player, menu),
                        pitch.getFloat(player, menu));
                return;
            }

            player.playSound(loc,
                    sound.getEnum(Sound.class, player, menu),
                    volume.getFloat(player, menu),
                    pitch.getFloat(player, menu));
        }
    }

    public static class Serializer implements NodeSerializer<ActionSound> {

        @Override
        public ActionSound deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if (node.isPrimitive()){
                String name = node.getString("null");
                try {
                    return new ActionSound(new TypeEnum<>(Sound.valueOf(name)),
                            new TypeFloat(1.0f),
                            new TypeFloat(1.0f),
                            new TypeBool(false),
                            null);
                } catch (IllegalArgumentException e){
                    if (!StringUtil.contains(name, '%'))
                        throw new NodeSerializeException(node, "Cannot read sound action with sound name '"+name+"'. Invalid sound name.");

                    return new ActionSound(new TypeEnum<>(name),
                            new TypeFloat(1.0f),
                            new TypeFloat(1.0f),
                            new TypeBool(false),
                            null);
                }
            }

            if (node.isMap()){
                String name = node.node("name").getString(node.getString());
                TypeEnum<Sound> sound;
                TypeFloat volume = new TypeFloat(1.0f);
                TypeFloat pitch = new TypeFloat(1.0f);
                TypeBool isPublic = new TypeBool(false);
                TypeLocation location = null;

                try {
                    sound = new TypeEnum<>(Sound.valueOf(name));
                } catch (IllegalArgumentException e1){
                    if (!StringUtil.contains(name, '%'))
                        throw new NodeSerializeException(node, "Cannot read sound action with sound name '"+name+"'. Invalid sound name.");
                    sound = new TypeEnum<>(name);
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

                return new ActionSound(sound, volume, pitch, isPublic, location);
            }

            throw new NodeSerializeException(node, "Cannot read sound action. Invalid format");
        }

    }
}
