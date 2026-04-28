package ru.abstractmenus.data.actions;


import ru.abstractmenus.datatype.TypeDouble;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.handler.EconomyHandler;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.AbstractMenusApi;

public class ActionMoneyGive implements Action {

    private final TypeDouble money;
    private final String provider;

    private ActionMoneyGive(TypeDouble money, String provider) {
        this.money = money;
        this.provider = provider;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        EconomyHandler eco = provider != null
                ? AbstractMenusApi.get().providers().economy().resolve(provider)
                : AbstractMenusApi.get().providers().economy().resolve();
        if (eco == null) {
            return;
        }
        eco.giveBalance(player, money.getDouble(player, menu));
    }

    public static class Serializer implements NodeSerializer<ActionMoneyGive> {

        @Override
        public ActionMoneyGive deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            MoneyAmountSpec spec = MoneyAmountSpec.parse(node);
            return new ActionMoneyGive(spec.amount, spec.provider);
        }

    }
}
