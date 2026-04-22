package ru.abstractmenus.data.rules;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.datatype.TypeInt;

public class RuleFoodLevel implements Rule {

    private final TypeInt foodLevel;

    private RuleFoodLevel(TypeInt foodLevel){
        this.foodLevel = foodLevel;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        return player.getFoodLevel() >= foodLevel.getInt(player, menu);
    }

    public static class Serializer implements NodeSerializer<RuleFoodLevel> {

        @Override
        public RuleFoodLevel deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleFoodLevel(node.getValue(TypeInt.class));
        }
    }
}
