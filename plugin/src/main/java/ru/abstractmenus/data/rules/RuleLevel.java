package ru.abstractmenus.data.rules;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.datatype.TypeInt;

public class RuleLevel implements Rule {

    private final TypeInt level;

    private RuleLevel(TypeInt level){
        this.level = level;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        return Handlers.getLevelHandler().getLevel(player) >= level.getInt(player, menu);
    }

    public static class Serializer implements NodeSerializer<RuleLevel> {

        @Override
        public RuleLevel deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleLevel(node.getValue(TypeInt.class));
        }

    }
}
