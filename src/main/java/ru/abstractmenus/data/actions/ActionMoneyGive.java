package ru.abstractmenus.data.actions;


import ru.abstractmenus.datatype.TypeDouble;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;

public class ActionMoneyGive implements Action {

    private final TypeDouble money;

    private ActionMoneyGive(TypeDouble money){
        this.money = money;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if(Handlers.getEconomyHandler() != null){
            Handlers.getEconomyHandler().giveBalance(player, money.getDouble(player, menu));
        }
    }

    public static class Serializer implements NodeSerializer<ActionMoneyGive> {

        @Override
        public ActionMoneyGive deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionMoneyGive(node.getValue(TypeDouble.class));
        }

    }
}
