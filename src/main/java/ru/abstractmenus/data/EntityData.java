package ru.abstractmenus.data;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.EntityType;

public class EntityData {

    private final EntityType type;
    private final String name;

    public EntityData(EntityType type, String name){
        this.type = type;
        this.name = name;
    }

    public EntityType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static class Serializer implements NodeSerializer<EntityData> {

        @Override
        public EntityData deserialize(Class token, ConfigNode node) throws NodeSerializeException {
            String typeName = node.node("type").getString();
            EntityType type;

            try {
                type = EntityType.valueOf(typeName);
            } catch (Exception e) {
                throw new NodeSerializeException(node.node("type"), "No entity type '"+typeName+"'");
            }

            return new EntityData(type, node.node("name").getString());
        }
    }
}
