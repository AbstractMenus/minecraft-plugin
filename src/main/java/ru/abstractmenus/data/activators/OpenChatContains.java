package ru.abstractmenus.data.activators;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.util.bukkit.BukkitTasks;

import java.util.List;

public class OpenChatContains extends Activator {

    private final List<String> messages;

    private OpenChatContains(List<String> messages){
        this.messages = messages;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        for(String str : messages){
            String msg = Handlers.getPlaceholderHandler().replace(event.getPlayer(), str);

            if(event.getMessage().contains(msg)){
                BukkitTasks.runTask(()->openMenu(null, event.getPlayer()));
                return;
            }
        }
    }

    public static class Serializer implements NodeSerializer<OpenChatContains> {

        @Override
        public OpenChatContains deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenChatContains(node.getList(String.class));
        }

    }

}
