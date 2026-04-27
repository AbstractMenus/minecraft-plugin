package ru.abstractmenus.data.actions;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.util.MiniMessageUtil;
import ru.abstractmenus.util.bukkit.BukkitTasks;

import java.util.List;

public class ActionPlayerChat implements Action {

    private final List<String> messages;

    private ActionPlayerChat(List<String> messages) {
        this.messages = messages;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        // player.chat() requires the player's region thread on Folia. The
        // activator that fires us may be running on the global scheduler
        // (e.g., from a chained chat-input action), so dispatch via the
        // entity scheduler.
        BukkitTasks.runForEntity(player, () -> messages.forEach(player::chat));
    }

    public static class Serializer implements NodeSerializer<ActionPlayerChat> {

        @Override
        public ActionPlayerChat deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionPlayerChat(MiniMessageUtil.parseToLegacy(
                    node.getList(String.class)
            ));
        }

    }
}
