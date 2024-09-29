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

public class RuleAnd implements Rule {

    private final List<Group> groups;

    private RuleAnd(List<Group> groups) {
        this.groups = groups;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        boolean result = true;
        if(groups != null) {
            for(RuleAnd.Group group : groups) {
                if(!group.check(player, menu, clickedItem))
                    result = false;
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
                    if(!rule.check(player, menu, clickedItem)) {
                        group.execDenyActions(player, menu, clickedItem);
                        return false;
                    }
                }
            }

            group.execActions(player, menu, clickedItem);

            return true;
        }

    }

    public static class Serializer implements NodeSerializer<RuleAnd> {

        @Override
        public RuleAnd deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            List<RulesGroup> groups = node.getList(RulesGroup.class);

            return new RuleAnd(groups.stream()
                    .map(RuleAnd.Group::new)
                    .collect(Collectors.toList()));
        }
    }
}