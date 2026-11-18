package ru.abstractmenus.data.activators;


import io.papermc.paper.event.player.AsyncChatEvent;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.event.EventHandler;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.util.bukkit.BukkitTasks;

import java.util.List;

public class OpenChatContains extends Activator {

    private final List<String> messages;

    private OpenChatContains(List<String> messages) {
        this.messages = messages;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        String message = event.signedMessage().message();
        var player = event.getPlayer();
        BukkitTasks.runForEntity(player, () -> {
            for (String str : messages) {
                String replaced = AbstractMenusApi.get().providers().placeholders().resolve().replace(player, str);
                if (message.contains(replaced)) {
                    openMenu(null, player);
                    return;
                }
            }
        });
    }

    public static class Serializer implements NodeSerializer<OpenChatContains> {

        @Override
        public OpenChatContains deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenChatContains(node.getList(String.class));
        }

    }

}
