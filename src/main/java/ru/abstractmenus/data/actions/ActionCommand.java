package ru.abstractmenus.data.actions;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActionCommand implements Action {

    private boolean isIgnorePlaceholder = false;
    private List<String> playerCommands = new ArrayList<>();
    private List<String> consoleCommands = new ArrayList<>();

    private void setPlayerCommands(List<String> playerCommands) {
        this.playerCommands = playerCommands;
    }

    private void setConsoleCommands(List<String> consoleCommands) {
        this.consoleCommands = consoleCommands;
    }

    private void setIgnorePlaceholder(boolean isIgnorePlaceholder) {
        this.isIgnorePlaceholder = isIgnorePlaceholder;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        for (String command : playerCommands) {
            if (command != null) {
                String resultCommand = isIgnorePlaceholder ? command : Handlers.getPlaceholderHandler().replace(player, command);
                player.chat("/" + Handlers.getPlaceholderHandler().replace(player, resultCommand));
            }
        }

        if (!consoleCommands.isEmpty()) {
            for (String command : consoleCommands) {
                if (command != null) {
                    String resultCommand = isIgnorePlaceholder ? command : Handlers.getPlaceholderHandler().replace(player, command);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resultCommand);
                }
            }
        }
    }

    public static class Serializer implements NodeSerializer<ActionCommand> {

        @Override
        public ActionCommand deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            ActionCommand action = new ActionCommand();
            ConfigNode playerNode = node.node("player");
            ConfigNode consoleNode = node.node("console");

            if (playerNode.rawValue() != null) {
                if (playerNode.isList()) {
                    action.setPlayerCommands(playerNode.getList(String.class));
                } else {
                    action.setPlayerCommands(Arrays.asList(playerNode.getString()));
                }
            }

            if (consoleNode.rawValue() != null) {
                if (consoleNode.isList()) {
                    action.setConsoleCommands(consoleNode.getList(String.class));
                } else {
                    action.setConsoleCommands(Arrays.asList(consoleNode.getString()));
                }
            }

            if (node.node("ignorePlaceholder").rawValue() != null) {
                action.setIgnorePlaceholder(node.node("ignorePlaceholder").getBoolean());
            }

            return action;
        }

    }
}
