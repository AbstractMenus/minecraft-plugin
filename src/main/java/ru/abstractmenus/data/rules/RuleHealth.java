package ru.abstractmenus.data.rules;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.datatype.TypeFloat;

public class RuleHealth implements Rule {

    private final TypeFloat health;

    private RuleHealth(TypeFloat health){
        this.health = health;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        return player.getHealth() >= health.getFloat(player, menu);
    }

    public static class Serializer implements NodeSerializer<RuleHealth> {

        @Override
        public RuleHealth deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleHealth(node.getValue(TypeFloat.class));
        }

    }
}
