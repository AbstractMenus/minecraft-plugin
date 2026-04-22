package ru.abstractmenus.data.catalogs;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Catalog;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.datatype.TypeWorld;
import ru.abstractmenus.extractors.EntityExtractor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class EntityCatalog implements Catalog<Entity> {

    private final TypeWorld world;
    private final List<EntityType> types;

    public EntityCatalog(TypeWorld world, List<EntityType> types) {
        this.world = world;
        this.types = types;
    }

    @Override
    public Collection<Entity> snapshot(Player player, Menu menu) {
        Collection<Entity> entities = player.getWorld().getEntities();
        if (world != null) {
            entities = world.getWorld(player, menu).getEntities();
        }
        return filter(entities);
    }

    private Collection<Entity> filter(Collection<Entity> entities) {
        if (types == null || types.isEmpty()) return entities;
        return entities.stream()
                .filter(entity -> types.contains(entity.getType()))
                .collect(Collectors.toList());
    }

    @Override
    public ValueExtractor extractor() {
        return EntityExtractor.INSTANCE;
    }

    public static class Serializer implements NodeSerializer<EntityCatalog> {

        @Override
        public EntityCatalog deserialize(Class<EntityCatalog> type, ConfigNode node) throws NodeSerializeException {
            TypeWorld world = node.node("world").getValue(TypeWorld.class);
            List<EntityType> types = node.node("allowedTypes").getList(EntityType.class);
            return new EntityCatalog(world, types);
        }
    }
}
