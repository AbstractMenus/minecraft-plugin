package ru.abstractmenus.data.rules;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.datatype.TypeInt;

import java.util.concurrent.ThreadLocalRandom;

public class RuleChance implements Rule {

    private final TypeInt chance;

    private RuleChance(TypeInt chance){
        this.chance = chance;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        return ThreadLocalRandom.current().nextInt(100) <= chance.getInt(player, menu);
    }

    public static class Serializer implements NodeSerializer<RuleChance> {

        @Override
        public RuleChance deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleChance(node.getValue(TypeInt.class));
        }
    }
}
