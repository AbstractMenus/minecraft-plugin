package ru.abstractmenus.data.activators;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.datatype.TypeMaterial;
import ru.abstractmenus.extractors.BlockExtractor;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.List;

public class OpenClickBlockType extends Activator {

    private final List<TypeMaterial> types;

    private OpenClickBlockType(List<TypeMaterial> types) {
        this.types = types;
    }

    @EventHandler
    public void onClickBlock(PlayerInteractEvent event) {
        if (!ActivatorUtil.checkHand(event)) return;

        if (event.getClickedBlock() != null) {
            for (TypeMaterial type : types) {
                Material mat = type.getMaterial(event.getPlayer(), null);

                if (event.getClickedBlock().getType().equals(mat)) {
                    event.setCancelled(true);
                    openMenu(event.getClickedBlock(), event.getPlayer());
                    return;
                }
            }
        }
    }

    @Override
    public ValueExtractor getValueExtractor() {
        return BlockExtractor.INSTANCE;
    }

    public static class Serializer implements NodeSerializer<OpenClickBlockType> {

        @Override
        public OpenClickBlockType deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenClickBlockType(node.getList(TypeMaterial.class));
        }

    }
}
