package ru.abstractmenus.data.activators;

import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.datatype.TypeLocation;
import ru.abstractmenus.extractors.BlockExtractor;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.abstractmenus.api.Activator;

import java.util.List;

public class OpenClickBlock extends Activator {

    private final List<TypeLocation> location;

    private OpenClickBlock(List<TypeLocation> location) {
        this.location = location;
    }

    @EventHandler
    public void onClickBlock(PlayerInteractEvent event) {
        if (!ActivatorUtil.checkHand(event)) return;

        if(event.getClickedBlock() != null) {
            for (TypeLocation loc : location) {
                if(event.getClickedBlock().getLocation().equals(loc.getLocation(event.getPlayer(), null))) {
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

    public static class Serializer implements NodeSerializer<OpenClickBlock> {

        @Override
        public OpenClickBlock deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenClickBlock(node.getList(TypeLocation.class));
        }

    }
}
