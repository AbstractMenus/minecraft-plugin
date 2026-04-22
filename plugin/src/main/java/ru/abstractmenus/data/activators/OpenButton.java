package ru.abstractmenus.data.activators;

import org.bukkit.Location;
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

public class OpenButton extends Activator {

    private final List<TypeLocation> location;

    private OpenButton(List<TypeLocation> location) {
        this.location = location;
    }

    @EventHandler
    public void onButtonClick(PlayerInteractEvent event) {
        if (!ActivatorUtil.checkHand(event) || event.getClickedBlock() == null) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK ||
                !event.getClickedBlock().getType().name().contains("BUTTON")) {
            return;
        }

        Location clickedLocation = event.getClickedBlock().getLocation();
        Player player = event.getPlayer();

        for (TypeLocation loc : location) {
            if (clickedLocation.equals(loc.getLocation(player, null))) {
                openMenu(event.getClickedBlock(), player);
                return;
            }
        }
    }


    @Override
    public ValueExtractor getValueExtractor() {
        return BlockExtractor.INSTANCE;
    }

    public static class Serializer implements NodeSerializer<OpenButton> {

        @Override
        public OpenButton deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenButton(node.getList(TypeLocation.class));
        }

    }

}
