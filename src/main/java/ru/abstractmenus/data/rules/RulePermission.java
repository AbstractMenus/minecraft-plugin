package ru.abstractmenus.data.rules;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;

import java.util.List;

public class RulePermission implements Rule {

    private final List<String> permission;

    private RulePermission(List<String> permission){
        this.permission = permission;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        for (String perm : permission){
            String replaced = Handlers.getPlaceholderHandler().replace(player, perm);
            if(!Handlers.getPermissionsHandler().hasPermission(player, replaced)) return false;
        }
        return true;
    }

    public static class Serializer implements NodeSerializer<RulePermission> {

        @Override
        public RulePermission deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RulePermission(node.getList(String.class));
        }

    }
}
