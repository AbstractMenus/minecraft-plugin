package ru.abstractmenus.data;

import org.jetbrains.annotations.NotNull;
import ru.abstractmenus.Constants;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Types;
import ru.abstractmenus.data.rules.logical.RuleAnd;
import ru.abstractmenus.util.StringUtil;

import java.util.*;
import java.util.function.Consumer;

public final class Actions implements Iterable<Action> {

    private List<Action> localActions;
    private Rule rules;
    private Actions actions;
    private Actions denyActions;

    public void add(Action action) {
        if (action == null) return;

        if (localActions == null)
            localActions = new ArrayList<>();

        localActions.add(action);
    }

    @NotNull
    @Override
    public Iterator<Action> iterator() {
        return localActions.iterator();
    }

    @Override
    public void forEach(Consumer<? super Action> consumer) {
        localActions.forEach(consumer);
    }

    @Override
    public Spliterator<Action> spliterator() {
        return localActions.spliterator();
    }

    public void setRules(Rule rules){
        this.rules = rules;
    }

    public void setActions(Actions actions){
        this.actions = actions;
    }

    public void setDenyActions(Actions actions){
        this.denyActions = actions;
    }

    public static boolean isReserved(String key) {
        return key.equals(Constants.FIELD_RULES)
                || key.equals(Constants.FIELD_ACTIONS)
                || key.equals(Constants.FIELD_DENY_ACTIONS);
    }

    public void activate(Player player, Menu menu, Item clickedItem){
        try {
            if (localActions != null) {
                for (Action action : localActions) {
                    action.activate(player, menu, clickedItem);
                }
            }

            if (rules != null) {
                if (rules.check(player, menu, clickedItem)) {
                    if (actions != null) actions.activate(player, menu, clickedItem);
                } else {
                    if (denyActions != null) denyActions.activate(player, menu, clickedItem);
                }
            } else {
                if (actions != null) actions.activate(player, menu, clickedItem);
            }
        } catch (Throwable t) {
            //t.printStackTrace();
            Logger.severe("Cannot execute some actions of list: " + t.getMessage());
        }
    }

    public static class Serializer implements NodeSerializer<Actions> {

        @Override
        public Actions deserialize(Class t, ConfigNode node) throws NodeSerializeException {
            Map<String, ConfigNode> activators = node.childrenMap();
            Actions actions = new Actions();

            for (Map.Entry<String, ConfigNode> entry : activators.entrySet()) {
                deserializeAction(entry.getKey(), entry.getValue(), actions);
            }

            if (node.node(Constants.FIELD_RULES).rawValue() != null) {
                actions.setRules(node.node(Constants.FIELD_RULES).getValue(RuleAnd.class));
            }

            if (node.node(Constants.FIELD_ACTIONS).rawValue() != null) {
                actions.setActions(node.node(Constants.FIELD_ACTIONS).getValue(Actions.class));
            }

            if (node.node(Constants.FIELD_DENY_ACTIONS).rawValue() != null) {
                actions.setDenyActions(node.node(Constants.FIELD_DENY_ACTIONS).getValue(Actions.class));
            }

            return actions;
        }

        public static void deserializeAction(String key, ConfigNode node, Actions actions) throws NodeSerializeException {
            key = StringUtil.replaceKeyPrefix(key);

            if (Actions.isReserved(key)) return;

            Class<? extends Action> type = Types.getActionType(key);

            if (type != null) {
                actions.add(node.getValue(type));
                return;
            }

            Logger.severe(String.format("Action '%s' is not exists", key));
        }
    }
}
