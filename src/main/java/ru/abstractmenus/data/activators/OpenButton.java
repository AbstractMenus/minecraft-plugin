package ru.abstractmenus.data.activators;

import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.datatype.TypeLocation;
import ru.abstractmenus.extractors.BlockExtractor;

import java.util.List;

public class OpenButton extends Activator {

    private final List<TypeLocation> location;

    private OpenButton(List<TypeLocation> location) {
        this.location = location;
    }

    @EventHandler
    public void onButtonClick(PlayerInteractEvent event) {
        if (!ActivatorUtil.checkHand(event)) return;

        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && event.getClickedBlock().getType().toString().contains("BUTTON")){

            for (TypeLocation loc : location) {
                if(event.getClickedBlock().getLocation().equals(loc.getLocation(event.getPlayer(), null))){
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

    public static class Serializer implements NodeSerializer<OpenButton> {

        @Override
        public OpenButton deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenButton(node.getList(TypeLocation.class));
        }

    }

}
