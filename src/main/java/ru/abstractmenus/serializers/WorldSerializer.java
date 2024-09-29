package ru.abstractmenus.serializers;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldSerializer implements NodeSerializer<World> {

    @Override
    public World deserialize(Class<World> type, ConfigNode node) {
        return Bukkit.getWorld(node.getString());
    }
    
}
