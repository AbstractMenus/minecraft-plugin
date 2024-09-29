package ru.abstractmenus.datatype;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;

public class TypeWorld extends DataType {

    private World value;

    public TypeWorld(String value) {
        super(value);
    }

    public TypeWorld(World value) {
        super(null);
        this.value = value;
    }

    public World getWorld(Player player, Menu menu){
        return (value != null) ? value : Bukkit.getWorld(replaceFor(player, menu));
    }

    public static class Serializer implements NodeSerializer<TypeWorld> {

        public TypeWorld deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            try {
                World world = Bukkit.getWorld(node.getString());
                if (world != null) return new TypeWorld(world);
            } catch (Throwable ignore){ }

            return new TypeWorld(node.getString());
        }
    }
}
