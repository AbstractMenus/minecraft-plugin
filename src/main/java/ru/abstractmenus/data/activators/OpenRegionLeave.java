package ru.abstractmenus.data.activators;

import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.events.RegionLeaveEvent;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.event.EventHandler;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.extractors.RegionExtractor;

import java.util.List;

public class OpenRegionLeave extends Activator {

    private final List<String> regions;

    private OpenRegionLeave(List<String> regions) {
        this.regions = regions;
    }

    @EventHandler
    public void onRegionEnter(RegionLeaveEvent event) {
        List<String> regions = Handlers.getPlaceholderHandler().replace(event.getPlayer(), this.regions);

        if (regions.contains(event.getRegion().getId())) {
            openMenu(event.getRegion(), event.getPlayer());
        }
    }

    @Override
    public ValueExtractor getValueExtractor() {
        return RegionExtractor.INSTANCE;
    }

    public static class Serializer implements NodeSerializer<OpenRegionLeave> {

        @Override
        public OpenRegionLeave deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenRegionLeave(node.getList(String.class));
        }
    }
}
