package ru.abstractmenus.data.rules;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.handler.EconomyHandler;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.data.actions.MoneyAmountSpec;
import ru.abstractmenus.datatype.TypeDouble;

public class RuleMoney implements Rule {

    private final TypeDouble money;
    private final String provider;

    private RuleMoney(TypeDouble money, String provider){
        this.money = money;
        this.provider = provider;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        EconomyHandler eco = provider != null
                ? AbstractMenusApi.get().providers().economy().resolve(provider)
                : AbstractMenusApi.get().providers().economy().resolve();
        if (eco == null) {
            return false;
        }
        return eco.hasBalance(player, money.getDouble(player, menu));
    }

    public static class Serializer implements NodeSerializer<RuleMoney> {

        @Override
        public RuleMoney deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            MoneyAmountSpec spec = MoneyAmountSpec.parse(node);
            return new RuleMoney(spec.amount, spec.provider);
        }

    }
}
