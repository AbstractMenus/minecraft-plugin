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

public class OpenShiftClickEntity extends Activator {

    private final List<EntityData> entityData;

    private OpenShiftClickEntity(List<EntityData> entityData) {
        this.entityData = entityData;
    }

    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (!ActivatorUtil.checkHand(event) || !player.isSneaking()) {
            return;
        }

        Entity clickedEntity = event.getRightClicked();

        for (EntityData data : entityData) {
            if (!clickedEntity.getType().equals(data.getType())) {
                continue;
            }

            if (data.getName() == null) {
                String name = Handlers.getPlaceholderHandler().replace(player, data.getName());
                if (clickedEntity.getName().equalsIgnoreCase(name)) {
                    openMenu(clickedEntity, player);
                    return;
                }
            }
        }
    }


    @Override
    public ValueExtractor getValueExtractor() {
        return EntityExtractor.INSTANCE;
    }

    public static class Serializer implements NodeSerializer<OpenShiftClickEntity> {

        @Override
        public OpenShiftClickEntity deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenShiftClickEntity(node.getList(EntityData.class));
        }
    }
}
