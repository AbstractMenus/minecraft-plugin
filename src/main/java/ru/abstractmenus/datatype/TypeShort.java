package ru.abstractmenus.datatype;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;

public class TypeShort extends DataType {

    private short value;
    private boolean hasValue = false;

    public TypeShort(String value) {
        super(value);
    }

    public TypeShort(short value) {
        super(null);
        this.value = value;
        this.hasValue = true;
    }

    public short getShort(Player player, Menu menu) throws NumberFormatException {
        return (hasValue) ? value : Double.valueOf(replaceFor(player, menu)).shortValue();
    }

    public static class Serializer implements NodeSerializer<TypeShort> {

        public TypeShort deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if(node.rawValue() instanceof Integer) return new TypeShort((short)node.getInt());
            if(!hasPlaceholder(node.getString("")))
                throw new NodeSerializeException(node, "Number type haven't placeholder to replace");
            return new TypeShort(node.getString());
        }

    }
}
