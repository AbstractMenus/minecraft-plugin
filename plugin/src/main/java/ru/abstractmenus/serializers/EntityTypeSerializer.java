package ru.abstractmenus.serializers;

import org.bukkit.entity.EntityType;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

public class EntityTypeSerializer implements NodeSerializer<EntityType> {

    @Override
    public EntityType deserialize(Class<EntityType> type, ConfigNode node) throws NodeSerializeException {
        try {
            return EntityType.valueOf(node.getString());
        } catch (Throwable t) {
            throw new NodeSerializeException(node, "Invalid entity type");
        }
    }
}
