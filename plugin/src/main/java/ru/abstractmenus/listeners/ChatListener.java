package ru.abstractmenus.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.abstractmenus.data.actions.ActionInputChat;
import ru.abstractmenus.services.MenuManager;
import ru.abstractmenus.util.bukkit.BukkitTasks;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        ActionInputChat.InputAction action = MenuManager.instance()
                .getAndRemoveInputAction(event.getPlayer());

        if (action != null) {
            // input() invokes onInput/onCancel actions on the player —
            // open menus, give items, etc. On Folia those operations
            // require the player's region thread; runTask routes to the
            // global scheduler which is forbidden from touching entity state.
            BukkitTasks.runForEntity(event.getPlayer(),
                    () -> action.input(event.signedMessage().message()));
            event.setCancelled(true);
        }
    }

}
