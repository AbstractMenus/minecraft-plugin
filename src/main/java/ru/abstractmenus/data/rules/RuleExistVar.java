package ru.abstractmenus.data.rules;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.variables.VariableManagerImpl;

public class RuleExistVar implements Rule {

    private final String player;
    private final String name;

    private RuleExistVar(String player, String name){
        this.player = player;
        this.name = name;
    }

    @Override
    public boolean check(Player p, Menu menu, Item clickedItem) {
        String varName = Handlers.getPlaceholderHandler().replace(p, this.name);

        if(this.player == null) {
            return VariableManagerImpl.instance().getGlobal(varName) != null;
        } else {
            String varPlayer = Handlers.getPlaceholderHandler().replace(p, this.player);
            return VariableManagerImpl.instance().getPersonal(varPlayer, varName) != null;
        }
    }

    public static class Serializer implements NodeSerializer<RuleExistVar> {

        @Override
        public RuleExistVar deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if (node.isPrimitive()) {
                return new RuleExistVar(null, node.getString());
            } else {
                String player = node.node("player").getString(null);
                String name = node.node("name").getString(null);
                return new RuleExistVar(player, name);
            }
        }
    }

}
