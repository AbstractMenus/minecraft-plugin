package ru.abstractmenus.datatype;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;

public class TypeInt extends DataType {

    private int value;
    private boolean hasValue = false;

    public TypeInt(String value) {
        super(value);
    }

    public TypeInt(int value) {
        super(null);
        this.value = value;
        this.hasValue = true;
    }

    public int getInt(Player player, Menu menu) throws NumberFormatException {
        return (hasValue) ? value : Double.valueOf(replaceFor(player, menu)).intValue();
    }

    public static class Serializer implements NodeSerializer<TypeInt> {

        public TypeInt deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if(node.rawValue() instanceof Integer) return new TypeInt(node.getInt());
            if(!hasPlaceholder(node.getString("")))
                throw new NodeSerializeException(node, "Number type haven't placeholder to replace");
            return new TypeInt(node.getString());
        }
    }
}
