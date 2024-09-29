package ru.abstractmenus.datatype;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;

public class TypeByte extends DataType {

    private byte value;
    private boolean hasValue = false;

    public TypeByte(String value) {
        super(value);
    }

    public TypeByte(byte value) {
        super(null);
        this.value = value;
        this.hasValue = true;
    }

    public byte getByte(Player player, Menu menu) throws NumberFormatException {
        return (hasValue) ? value : Byte.parseByte(replaceFor(player, menu));
    }

    public static class Serializer implements NodeSerializer<TypeByte> {

        public TypeByte deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if(node.rawValue() instanceof Integer) return new TypeByte((byte)node.getInt());
            if(!hasPlaceholder(node.getString("")))
                throw new NodeSerializeException(node, "Number type haven't placeholder to replace");
            return new TypeByte(node.getString());
        }
    }
}
