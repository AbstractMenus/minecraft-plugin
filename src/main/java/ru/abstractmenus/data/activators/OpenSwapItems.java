package ru.abstractmenus.data.activators;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import ru.abstractmenus.api.Activator;

public class OpenSwapItems extends Activator {

    private OpenSwapItems() { }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void itemSwapEvent(PlayerSwapHandItemsEvent event){
        if (!event.isCancelled()) {
            openMenu(null, event.getPlayer());
            event.setCancelled(true);
        }
    }

    public static class Serializer implements NodeSerializer<OpenSwapItems>{

        @Override
        public OpenSwapItems deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenSwapItems();
        }

    }
}
