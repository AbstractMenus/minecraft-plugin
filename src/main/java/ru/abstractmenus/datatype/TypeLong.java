package ru.abstractmenus.datatype;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;

public class TypeLong extends DataType {

    private int value;
    private boolean hasValue = false;

    public TypeLong(String value) {
        super(value);
    }

    public TypeLong(int value) {
        super(null);
        this.value = value;
        this.hasValue = true;
    }

    public long getLong(Player player, Menu menu) throws NumberFormatException {
        return (hasValue) ? value : Double.valueOf(replaceFor(player, menu)).longValue();
    }

    public static class Serializer implements NodeSerializer<TypeLong> {

        public TypeLong deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if(node.rawValue() instanceof Integer || node.rawValue() instanceof Long) return new TypeLong(node.getInt());
            if(!hasPlaceholder(node.getString("")))
                throw new NodeSerializeException(node, "Number type haven't placeholder to replace");
            return new TypeLong(node.getString());
        }
    }
}
