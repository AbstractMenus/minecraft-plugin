package ru.abstractmenus.datatype;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;

public class TypeEnum<E extends Enum<E>> extends DataType {

    private E value = null;

    public TypeEnum(String value) {
        super(value);
    }

    public TypeEnum(E value) {
        super(null);
        this.value = value;
    }

    public E getEnum(Class<E> type, Player player, Menu menu) throws IllegalArgumentException {
        return (value != null) ? value : E.valueOf(type, replaceFor(player, menu).toUpperCase());
    }

    public static class Serializer implements NodeSerializer<TypeEnum> {

        public TypeEnum deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new TypeEnum(node.getString());
        }
    }
}
