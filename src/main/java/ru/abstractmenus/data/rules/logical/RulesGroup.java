package ru.abstractmenus.data.rules.logical;

import ru.abstractmenus.Constants;
import ru.abstractmenus.data.Actions;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.Types;
import ru.abstractmenus.util.StringUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RulesGroup {

    private final List<Rule> rules;
    private final Actions actions;
    private final Actions denyActions;

    public RulesGroup(List<Rule> rules, Actions actions, Actions denyActions) {
        this.rules = rules;
        this.actions = actions;
        this.denyActions = denyActions;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void execActions(Player player, Menu menu, Item clickedItem) {
        if (actions != null)
            actions.activate(player, menu, clickedItem);
    }

    public void execDenyActions(Player player, Menu menu, Item clickedItem) {
        if (denyActions != null)
            denyActions.activate(player, menu, clickedItem);
    }

    public static class Serializer implements NodeSerializer<RulesGroup> {

        @Override
        public RulesGroup deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            Map<String, ConfigNode> children = node.childrenMap();
            List<Rule> rules = null;
            Actions actions = null;
            Actions denyActions = null;

            for (Map.Entry<String, ConfigNode> entry : children.entrySet()) {
                String key = StringUtil.replaceKeyPrefix(entry.getKey());
                if (Actions.isReserved(key)) continue;
                Rule rule = getSingleRule(key, entry.getValue());

                if (rules == null)
                    rules = new LinkedList<>();

                rules.add(rule);
            }

            if (node.node(Constants.FIELD_ACTIONS).rawValue() != null) {
                actions = node.node(Constants.FIELD_ACTIONS).getValue(Actions.class);
            }

            if (node.node(Constants.FIELD_DENY_ACTIONS).rawValue() != null) {
                denyActions = node.node(Constants.FIELD_DENY_ACTIONS).getValue(Actions.class);
            }

            return new RulesGroup(rules, actions, denyActions);
        }

        private Rule getSingleRule(String key, ConfigNode node) throws NodeSerializeException {
            boolean isNot = false;

            if (key.charAt(0) == '-') {
                key = key.substring(1);
                isNot = true;
            }

            Class<? extends Rule> type = Types.getRuleType(key);

            if (type != null) {
                Rule rule = node.getValue(type);
                return isNot ? new RuleNot(rule) : rule;
            }

            throw new NodeSerializeException(node, "Rule with name " + key + " doesn't exists");
        }
    }
}
