package ru.abstractmenus.data.activators;

import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.datatype.TypeLocation;
import ru.abstractmenus.extractors.BlockExtractor;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.abstractmenus.api.Activator;

import java.util.List;

public class OpenLever extends Activator {

    private final List<TypeLocation> location;

    private OpenLever(List<TypeLocation> location){
        this.location = location;
    }

    @EventHandler
    public void onLeverClick(PlayerInteractEvent event) {
        if (!ActivatorUtil.checkHand(event)) return;

        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && event.getClickedBlock().getType().equals(Material.LEVER)) {
            for (TypeLocation loc : location) {
                if (event.getClickedBlock().getLocation().equals(loc.getLocation(event.getPlayer(), null))) {
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

    public static class Serializer implements NodeSerializer<OpenLever> {

        @Override
        public OpenLever deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenLever(node.getList(TypeLocation.class));
        }

    }

}
