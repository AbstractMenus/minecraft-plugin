package ru.abstractmenus.data.rules;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.variables.VariableManagerImpl;

public class RuleExistVarp implements Rule {

    private final String name;

    private RuleExistVarp(String name){
        this.name = name;
    }

    @Override
    public boolean check(Player p, Menu menu, Item clickedItem) {
        String varName = Handlers.getPlaceholderHandler().replace(p, this.name);
        return VariableManagerImpl.instance().getPersonal(p.getName(), varName) != null;
    }

    public static class Serializer implements NodeSerializer<RuleExistVarp> {

        @Override
        public RuleExistVarp deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleExistVarp(node.getString());
        }
    }

}
