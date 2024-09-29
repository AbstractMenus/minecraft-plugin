package ru.abstractmenus.datatype;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;

public class TypeLocation extends DataType {

    private Location value;
    private TypeWorld world;
    private TypeDouble x, y, z;
    private TypeFloat yaw, pitch;

    public TypeLocation(TypeWorld world, TypeDouble x, TypeDouble y, TypeDouble z, TypeFloat yaw, TypeFloat pitch) {
        super(null);

        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public TypeLocation(Location value) {
        super(null);
        this.value = value;
    }

    public Location getLocation(Player player, Menu menu) throws NumberFormatException {
        return (value != null) ? value : parseLocation(player, menu);
    }

    private Location parseLocation(Player player, Menu menu) throws NumberFormatException {
        return new Location(
                world.getWorld(player, menu),
                x.getDouble(player, menu),
                y.getDouble(player, menu),
                z.getDouble(player, menu),
                yaw.getFloat(player, menu),
                pitch.getFloat(player, menu)
        );
    }

    public static String locToString(Location location){
        return String.format("%s,%s,%s,%s,%s,%s",
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch());
    }

    public static class Serializer implements NodeSerializer<TypeLocation> {

        @Override
        public TypeLocation deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if(!node.isMap()){
                if(node.getString() != null){
                    return parseString(node, node.getString());
                }

                throw new NodeSerializeException(node, "Error while parsing location in node " + node.parent().key() + ". Inline location format invalid");
            }

            try{
                return parseNode(node);
            } catch (NodeSerializeException e){
                return new TypeLocation(parseNative(node));
            }
        }

        private TypeLocation parseNode(ConfigNode node) throws NodeSerializeException {
            TypeWorld world = new TypeWorld(Bukkit.getWorld("world"));
            TypeDouble x = node.node("x").getValue(TypeDouble.class);
            TypeDouble y = node.node("y").getValue(TypeDouble.class);
            TypeDouble z = node.node("z").getValue(TypeDouble.class);
            TypeFloat yaw = new TypeFloat(0.0f);
            TypeFloat pitch = new TypeFloat(0.0f);

            if(node.node("world").rawValue() != null){
                world = node.node("world").getValue(TypeWorld.class);
            }

            if(node.node("yaw").rawValue() != null){
                yaw = node.node("yaw").getValue(TypeFloat.class);
            }

            if(node.node("pitch").rawValue() != null){
                pitch = node.node("pitch").getValue(TypeFloat.class);
            }

            return new TypeLocation(world, x, y, z, yaw, pitch);
        }

        private Location parseNative(ConfigNode node) throws NodeSerializeException {
            World world = Bukkit.getWorld("world");
            double x = node.node("x").getDouble();
            double y = node.node("y").getDouble();
            double z = node.node("z").getDouble();
            float yaw = 0.0f;
            float pitch = 0.0f;

            if(node.node("world").rawValue() != null){
                world = node.node("world").getValue(World.class);

                if(world == null){
                    throw new NodeSerializeException(node, "Error while parsing location in node " + node.parent().key() + ". World '"+node.node("world").getString()+"' not exists");
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

        private TypeLocation parseString(ConfigNode node, String str) throws NodeSerializeException {
            TypeWorld world = new TypeWorld(Bukkit.getWorlds().get(0));
            TypeDouble x = new TypeDouble(0.0);
            TypeDouble y = new TypeDouble(0.0);
            TypeDouble z = new TypeDouble(0.0);
            TypeFloat yaw = new TypeFloat(0.0f);
            TypeFloat pitch = new TypeFloat(0.0f);

            String[] args = str.split(",");

            if(args.length < 3){
                throw new NodeSerializeException(node, "Error while parsing location. Minimal arguments count: 3 (x,y,z)");
            }

            if(args.length == 3){
                x = new TypeDouble(args[0].trim());
                y = new TypeDouble(args[1].trim());
                z = new TypeDouble(args[2].trim());
            }

            if(args.length == 4){
                world = new TypeWorld(args[0].trim());
                x = new TypeDouble(args[1].trim());
                y = new TypeDouble(args[2].trim());
                z = new TypeDouble(args[3].trim());
            }

            if(args.length == 6){
                world = new TypeWorld(args[0].trim());
                x = new TypeDouble(args[1].trim());
                y = new TypeDouble(args[2].trim());
                z = new TypeDouble(args[3].trim());
                yaw = new TypeFloat(args[4].trim());
                pitch = new TypeFloat(args[5].trim());
            }

            return new TypeLocation(world, x, y, z, yaw, pitch);
        }
    }
}
