package ru.abstractmenus.data.actions;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.variables.Var;
import ru.abstractmenus.data.Actions;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.services.MenuManager;
import ru.abstractmenus.variables.VarImpl;
import ru.abstractmenus.variables.VariableManagerImpl;

public class ActionInputChat implements Action {

    private final String varName;
    private final boolean global;
    private final String cancelWord;
    private final Actions onInput;
    private final Actions onCancel;

    public ActionInputChat(String varName, boolean global, String cancelWord,
                           Actions onInput, Actions onCancel) {
        this.varName = varName;
        this.global = global;
        this.cancelWord = cancelWord;
        this.onInput = onInput;
        this.onCancel = onCancel;
    }

    @Override
    public void activate(Player player, Menu m, Item clickedItem) {
        String name = Handlers.getPlaceholderHandler().replace(player, varName);
        InputAction action = new InputAction(player, name, global, cancelWord, onInput, onCancel);
        MenuManager.instance().saveInputAction(action);
        m.close(player);
    }

    public static class InputAction {

        private final Player player;
        private final String varName;
        private final boolean global;
        private final String cancelWord;
        private final Actions onInput;
        private final Actions onCancel;

        public InputAction(Player player, String varName,
                           boolean global, String cancelWord,
                           Actions onInput, Actions onCancel) {
            this.player = player;
            this.varName = varName;
            this.global = global;
            this.cancelWord = cancelWord;
            this.onInput = onInput;
            this.onCancel = onCancel;
        }

        public Player getPlayer() {
            return player;
        }

        public void input(String message) {
            if (message.equalsIgnoreCase(cancelWord)) {
                if (onCancel != null)
                    onCancel.activate(player, null, null);
                return;
            }

            Var var = new VarImpl(varName, message, 0);

            if (global) {
                VariableManagerImpl.instance()
                        .saveGlobal(var);
            } else {
                VariableManagerImpl.instance()
                        .savePersonal(player.getName(), var);
            }

            if (onInput != null)
                onInput.activate(player, null, null);
        }
    }

    public static class Serializer implements NodeSerializer<ActionInputChat> {

        @Override
        public ActionInputChat deserialize(Class<ActionInputChat> type, ConfigNode node) throws NodeSerializeException {
            String varName = node.node("into").getString();
            boolean global = node.node("global").getBoolean(false);
            String cancelWord = node.node("cancelOn").getString(null);
            Actions onInput = node.node("onInput").getValue(Actions.class, null);
            Actions onCancel = node.node("onCancel").getValue(Actions.class, null);
            return new ActionInputChat(varName, global, cancelWord, onInput, onCancel);
        }

    }
}
