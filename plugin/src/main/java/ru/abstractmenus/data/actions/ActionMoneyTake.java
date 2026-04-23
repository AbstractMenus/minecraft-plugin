package ru.abstractmenus.data.actions;


import ru.abstractmenus.datatype.TypeDouble;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.AbstractMenusApi;

public class ActionMoneyTake implements Action {

    private final TypeDouble money;

    private ActionMoneyTake(TypeDouble money) {
        this.money = money;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if (AbstractMenusApi.get().providers().economy() != null) {
            AbstractMenusApi.get().providers().economy().takeBalance(player, money.getDouble(player, menu));
        }
    }

    public static class Serializer implements NodeSerializer<ActionMoneyTake> {

        @Override
        public ActionMoneyTake deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionMoneyTake(node.getValue(TypeDouble.class));
        }

    }
}
