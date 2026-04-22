package ru.abstractmenus.data.activators;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.datatype.TypeLocation;
import ru.abstractmenus.extractors.BlockExtractor;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.List;

public class OpenPlate extends Activator {

    private final List<TypeLocation> location;

    private OpenPlate(List<TypeLocation> location) {
        this.location = location;
    }

    @EventHandler
    public void onPlateEnter(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (!ActivatorUtil.checkHand(event) || clickedBlock == null) {
            return;
        }

        if (event.getAction() != Action.PHYSICAL || !clickedBlock.getType().name().contains("PLATE")) {
            return;
        }

        Location clickedLocation = clickedBlock.getLocation();
        Player player = event.getPlayer();

        for (TypeLocation loc : location) {
            if (clickedLocation.equals(loc.getLocation(player, null))) {
                openMenu(clickedBlock, player);
                return;
            }
        }
    }


    @Override
    public ValueExtractor getValueExtractor() {
        return BlockExtractor.INSTANCE;
    }

    public static class Serializer implements NodeSerializer<OpenPlate> {

        @Override
        public OpenPlate deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenPlate(node.getList(TypeLocation.class));
        }

    }

}
