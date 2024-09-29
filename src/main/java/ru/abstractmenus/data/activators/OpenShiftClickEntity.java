package ru.abstractmenus.data.activators;

import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.data.EntityData;
import ru.abstractmenus.extractors.EntityExtractor;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.Handlers;

import java.util.List;

public class OpenShiftClickEntity extends Activator {

    private final List<EntityData> entityData;

    private OpenShiftClickEntity(List<EntityData> entityData) {
        this.entityData = entityData;
    }

    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent event){
        if (!ActivatorUtil.checkHand(event) || !event.getPlayer().isSneaking()) return;

        for (EntityData data : entityData) {
            if (event.getRightClicked().getType().equals(data.getType())){
                if (data.getName() != null){
                    String name = Handlers.getPlaceholderHandler().replace(event.getPlayer(), data.getName());

                    if (event.getRightClicked().getName().equalsIgnoreCase(name)){
                        openMenu(event.getRightClicked(), event.getPlayer());
                        return;
                    }

                    continue;
                }

                openMenu(event.getRightClicked(), event.getPlayer());
                return;
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
