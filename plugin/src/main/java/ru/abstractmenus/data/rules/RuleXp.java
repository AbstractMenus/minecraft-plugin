package ru.abstractmenus.data.rules;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.datatype.TypeFloat;

public class RuleXp implements Rule {

    private final TypeFloat xp;

    private RuleXp(TypeFloat xp) {
        this.xp = xp;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        return Handlers.getLevelHandler().getXp(player) >= xp.getFloat(player, menu);
    }

    public static class Serializer implements NodeSerializer<RuleXp> {

        @Override
        public RuleXp deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleXp(node.getValue(TypeFloat.class));
        }
    }
}
