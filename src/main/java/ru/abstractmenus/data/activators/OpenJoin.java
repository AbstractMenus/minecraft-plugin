package ru.abstractmenus.data.activators;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.util.bukkit.BukkitTasks;

public class OpenJoin extends Activator {

    private final boolean open;

    private OpenJoin(boolean value){
        this.open = value;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if(open){
            BukkitTasks.runTaskLater(()->openMenu(null, event.getPlayer()), 20L);
        }
    }

    public static class Serializer implements NodeSerializer<OpenJoin> {

        @Override
        public OpenJoin deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenJoin(node.getBoolean());
        }

    }
}
