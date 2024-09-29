package ru.abstractmenus.data.rules.logical;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.List;
import java.util.stream.Collectors;

public class RuleOneOf implements Rule {

    private final List<RuleAnd.Group> groups;

    public RuleOneOf(List<RuleAnd.Group> groups) {
        this.groups = groups;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        if (groups != null) {
            for (RuleAnd.Group group : groups) {
                if (group.check(player, menu, clickedItem))
                    return true;
            }
        }
        return false;
    }

    public static class Serializer implements NodeSerializer<RuleOneOf> {

        @Override
        public RuleOneOf deserialize(Class<RuleOneOf> type, ConfigNode node) throws NodeSerializeException {
            List<RulesGroup> groups = node.getList(RulesGroup.class);

            return new RuleOneOf(groups.stream()
                    .map(RuleAnd.Group::new)
                    .collect(Collectors.toList()));
        }
    }
}
