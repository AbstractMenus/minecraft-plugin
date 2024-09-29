package ru.abstractmenus.datatype;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;

public class TypeFloat extends DataType {

    private float value;
    private boolean hasValue = false;

    public TypeFloat(String value) {
        super(value);
    }

    public TypeFloat(float value) {
        super(null);
        this.value = value;
        this.hasValue = true;
    }

    public float getFloat(Player player, Menu menu) throws NumberFormatException {
        return (hasValue) ? value : Float.parseFloat(replaceFor(player, menu));
    }

    public static class Serializer implements NodeSerializer<TypeFloat> {

        public TypeFloat deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if(node.rawValue() instanceof Number) return new TypeFloat(node.getFloat());
            if(!hasPlaceholder(node.getString("")))
                throw new NodeSerializeException(node, "Number type haven't placeholder to replace");
            return new TypeFloat(node.getString());
        }
    }
}
