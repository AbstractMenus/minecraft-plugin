package ru.abstractmenus.data.rules;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.AbstractMenusApi;

import java.util.List;

public class RuleGroup implements Rule {

    private final List<String> groups;

    private RuleGroup(List<String> groups) {
        this.groups = groups;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        for (String group : groups) {
            String replaced = AbstractMenusApi.get().providers().placeholders().resolve().replace(player, group);
            if (!AbstractMenusApi.get().providers().permissions().resolve().hasGroup(player, replaced)) return false;
        }
        return true;
    }

    public static class Serializer implements NodeSerializer<RuleGroup> {

        @Override
        public RuleGroup deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleGroup(node.getList(String.class));
        }
    }
}
