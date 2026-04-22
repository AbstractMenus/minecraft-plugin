package ru.abstractmenus.data.activators;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.data.EntityData;
import ru.abstractmenus.extractors.EntityExtractor;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.List;

public class OpenClickEntity extends Activator {

    private final List<EntityData> entityData;

    private OpenClickEntity(List<EntityData> entityData) {
        this.entityData = entityData;
    }

    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent event) {
        if (!ActivatorUtil.checkHand(event)) {
            return;
        }

        Entity clickedEntity = event.getRightClicked();
        Player player = event.getPlayer();

        for (EntityData data : entityData) {
            if (!clickedEntity.getType().equals(data.getType())) {
                continue;
            }

            if (data.getName() != null) {
                String expectedName = Handlers.getPlaceholderHandler().replace(player, data.getName());
                if (!clickedEntity.getName().equalsIgnoreCase(expectedName)) {
                    continue;
                }
            }

            openMenu(clickedEntity, player);
            return;
        }
    }


    @Override
    public ValueExtractor getValueExtractor() {
        return EntityExtractor.INSTANCE;
    }

    public static class Serializer implements NodeSerializer<OpenClickEntity> {

        @Override
        public OpenClickEntity deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenClickEntity(node.getList(EntityData.class));
        }

    }
}
