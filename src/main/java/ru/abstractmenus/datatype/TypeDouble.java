package ru.abstractmenus.datatype;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;

public class TypeDouble extends DataType {

    private double value;
    private boolean hasValue = false;

    public TypeDouble(String value) {
        super(value);
    }

    public TypeDouble(double value) {
        super(null);
        this.value = value;
        this.hasValue = true;
    }

    public double getDouble(Player player, Menu menu) throws NumberFormatException {
        return (hasValue) ? value : Double.parseDouble(replaceFor(player, menu));
    }

    public static class Serializer implements NodeSerializer<TypeDouble> {

        public TypeDouble deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if(node.rawValue() instanceof Number) return new TypeDouble(node.getDouble());
            if(!hasPlaceholder(node.getString("")))
                throw new NodeSerializeException(node, "Number type haven't placeholder to replace");
            return new TypeDouble(node.getString());
        }
    }
}
