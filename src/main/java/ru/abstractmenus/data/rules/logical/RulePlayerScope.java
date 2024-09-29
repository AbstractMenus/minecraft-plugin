package ru.abstractmenus.data.rules.logical;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

public class RulePlayerScope implements Rule {

    private final String playerName;
    private final Rule rules;

    public RulePlayerScope(String playerName, Rule rules) {
        this.playerName = playerName;
        this.rules = rules;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        String replacedName = Handlers.getPlaceholderHandler().replace(player, playerName);
        Player target = Bukkit.getPlayerExact(replacedName);

        if (target != null && target.isOnline()) {
            return rules.check(target, menu, clickedItem);
        }

        return false;
    }

    public static class Serializer implements NodeSerializer<RulePlayerScope> {

        @Override
        public RulePlayerScope deserialize(Class<RulePlayerScope> type, ConfigNode node) throws NodeSerializeException {
            String playerName = node.node("name").getString();
            Rule actions = node.node("rules").getValue(RuleAnd.class);
            return new RulePlayerScope(playerName, actions);
        }
    }
}
