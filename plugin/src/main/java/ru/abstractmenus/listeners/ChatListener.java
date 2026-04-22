package ru.abstractmenus.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.abstractmenus.data.actions.ActionInputChat;
import ru.abstractmenus.services.MenuManager;
import ru.abstractmenus.util.bukkit.BukkitTasks;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        ActionInputChat.InputAction action = MenuManager.instance()
                .getAndRemoveInputAction(event.getPlayer());

        if (action != null) {
            BukkitTasks.runTask(() -> action.input(event.signedMessage().message()));
            event.setCancelled(true);
        }
    }

}
