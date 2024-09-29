package ru.abstractmenus.datatype;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;

public class TypeBool extends DataType {

    private static final String TRUE_VAL = "true";

    private boolean value;
    private boolean hasValue = false;

    public TypeBool(String value) {
        super(value);
    }

    public TypeBool(boolean value) {
        super(null);
        this.value = value;
        this.hasValue = true;
    }

    public boolean getBool(Player player, Menu menu){
        return (hasValue) ? value : TRUE_VAL.equalsIgnoreCase(replaceFor(player, menu));
    }

    public static class Serializer implements NodeSerializer<TypeBool> {

        public TypeBool deserialize(Class<TypeBool> type, ConfigNode node) throws NodeSerializeException {
            if (node.rawValue() instanceof Boolean)
                return new TypeBool(node.getBoolean());

            return new TypeBool(node.getString());
        }
    }
}
