package ru.abstractmenus.data.rules.logical;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;

import java.util.List;
import java.util.stream.Collectors;

public class RuleOr implements Rule {

    private final List<Group> groups;

    private RuleOr(List<Group> groups) {
        this.groups = groups;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        boolean result = false;
        if(groups != null) {
            for(RuleOr.Group group : groups) {
                if(group.check(player, menu, clickedItem))
                    result = true;
            }
        }
        return result;
    }

    public static class Group {

        private final RulesGroup group;

        public Group(RulesGroup group) {
            this.group = group;
        }

        public boolean check(Player player, Menu menu, Item clickedItem) {
            if(group.getRules() != null) {
                for(Rule rule : group.getRules()) {
                    if(rule.check(player, menu, clickedItem)) {
                        group.execActions(player, menu, clickedItem);
                        return true;
                    }
                }
            }

            group.execDenyActions(player, menu, clickedItem);

            return false;
        }

    }

    public static class Serializer implements NodeSerializer<RuleOr> {

        @Override
        public RuleOr deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            List<RulesGroup> groups = node.getList(RulesGroup.class);

            return new RuleOr(groups.stream()
                    .map(RuleOr.Group::new)
                    .collect(Collectors.toList()));
        }
    }
}