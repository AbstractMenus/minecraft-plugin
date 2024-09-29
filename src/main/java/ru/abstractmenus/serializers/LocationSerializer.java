package ru.abstractmenus.serializers;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationSerializer implements NodeSerializer<Location> {

    @Override
    public Location deserialize(Class<Location> type, ConfigNode node) throws NodeSerializeException {
        if(!node.isMap()) {
            if(node.getString() != null) {
                return parseString(node, node.getString());
            }

            throw new NodeSerializeException(node, "Error while parsing location in node " + node.parent().key() + ". Inline location format invalid");
        }

        return parseNode(node);
    }

    private Location parseNode(ConfigNode node) throws NodeSerializeException {
        World world = Bukkit.getWorld("world");
        double x = node.node("x").getDouble();
        double y = node.node("y").getDouble();
        double z = node.node("z").getDouble();
        float yaw = 0.0f;
        float pitch = 0.0f;

        if(node.node("world").rawValue() != null){
            world = node.node("world").getValue(World.class);

            if(world == null){
                throw new NodeSerializeException(node.node("world"), "Error while parsing location in node " + node.parent().key() + ". World '"+node.node("world").getString()+"' not exists");
            }
        }

        if(node.node("yaw").rawValue() != null){
            yaw = node.node("yaw").getFloat();
        }

        if(node.node("pitch").rawValue() != null){
            pitch = node.node("pitch").getFloat();
        }

        return new Location(world, x, y, z, yaw, pitch);
    }

    private Location parseString(ConfigNode node, String str) throws NodeSerializeException {
        World world = Bukkit.getWorld("world");
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        float yaw = 0.0f;
        float pitch = 0.0f;

        String[] args = str.split(",");

        if(args.length < 3){
            throw new NodeSerializeException(node, "Error while parsing location. Minimal arguments count: 3 (x,y,z)");
        }

        if(args.length == 3){
            x = Double.parseDouble(args[0].trim());
            y = Double.parseDouble(args[1].trim());
            z = Double.parseDouble(args[2].trim());
        }

        if(args.length == 4){
            world = Bukkit.getWorld(args[0].trim());
            x = Double.parseDouble(args[1].trim());
            y = Double.parseDouble(args[2].trim());
            z = Double.parseDouble(args[3].trim());
        }

        if(args.length == 6){
            world = Bukkit.getWorld(args[0].trim());
            x = Double.parseDouble(args[1].trim());
            y = Double.parseDouble(args[2].trim());
            z = Double.parseDouble(args[3].trim());
            yaw = Float.parseFloat(args[4].trim());
            pitch = Float.parseFloat(args[5].trim());
        }

        return new Location(world, x, y, z, yaw, pitch);
    }
}
