package ru.abstractmenus.data.rules;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.datatype.TypeInt;

public class RuleOnline implements Rule {

    private final TypeInt online;

    private RuleOnline(TypeInt online){
        this.online = online;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        return Bukkit.getOnlinePlayers().size() >= online.getInt(player, menu);
    }

    public static class Serializer implements NodeSerializer<RuleOnline> {

        @Override
        public RuleOnline deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleOnline(node.getValue(TypeInt.class));
        }
    }
}
