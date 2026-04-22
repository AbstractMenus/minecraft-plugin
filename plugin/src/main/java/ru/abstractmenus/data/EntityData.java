package ru.abstractmenus.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.EntityType;

@Getter
@RequiredArgsConstructor
public class EntityData {

    private final EntityType type;
    private final String name;

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
