package ru.abstractmenus.data.rules;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.datatype.TypeDouble;

public class RuleMoney implements Rule {

    private final TypeDouble money;

    private RuleMoney(TypeDouble money){
        this.money = money;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        if(Handlers.getEconomyHandler() != null){
            return Handlers.getEconomyHandler().hasBalance(player, money.getDouble(player, menu));
        }
        return false;
    }

    public static class Serializer implements NodeSerializer<RuleMoney> {

        @Override
        public RuleMoney deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleMoney(node.getValue(TypeDouble.class));
        }

    }
}
