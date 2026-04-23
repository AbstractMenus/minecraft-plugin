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

public class ActionMoneyTake implements Action {

    private final TypeDouble money;
    private final String provider;

    private ActionMoneyTake(TypeDouble money, String provider) {
        this.money = money;
        this.provider = provider;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        EconomyHandler eco = provider != null
                ? AbstractMenusApi.get().providers().economy(provider)
                : AbstractMenusApi.get().providers().economy();
        if (eco == null) {
            return;
        }
        eco.takeBalance(player, money.getDouble(player, menu));
    }

    public static class Serializer implements NodeSerializer<ActionMoneyTake> {

        @Override
        public ActionMoneyTake deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            TypeDouble money;
            String provider = null;

            if (node.isMap()) {
                money = node.node("amount").getValue(TypeDouble.class);
                provider = node.node("provider").getString(null);
            } else {
                money = node.getValue(TypeDouble.class);
            }

            if (provider != null) {
                if (!AbstractMenusApi.get().providers().hasEconomy(provider)) {
                    StringBuilder known = new StringBuilder();
                    for (EconomyHandler h : AbstractMenusApi.get().providers().allEconomy()) {
                        if (known.length() > 0) known.append(", ");
                        known.append(h.getClass().getSimpleName());
                    }
                    throw new NodeSerializeException(node,
                            "Unknown economy provider '" + provider + "'. Registered: ["
                                    + known + "]. Omit the 'provider' field for default selection.");
                }
            }

            return new ActionMoneyTake(money, provider);
        }

    }
}
