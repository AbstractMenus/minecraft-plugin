package ru.abstractmenus.data.rules;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

public class RulePlayerIsOnline implements Rule {

    private final String playerName;

    private RulePlayerIsOnline(String playerName){
        this.playerName = playerName;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        String replaced = Handlers.getPlaceholderHandler().replace(player, playerName);
        Player foundPlayer = Bukkit.getPlayerExact(replaced);

        return foundPlayer != null && foundPlayer.isOnline();
    }

    public static class Serializer implements NodeSerializer<RulePlayerIsOnline> {

        @Override
        public RulePlayerIsOnline deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RulePlayerIsOnline(node.getString());
        }
    }
}
