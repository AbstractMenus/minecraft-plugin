package ru.abstractmenus.data.actions;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.AbstractMenusApi;

public class ActionGroupAdd implements Action {

    private final String group;

    private ActionGroupAdd(String group) {
        this.group = group;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        String group = AbstractMenusApi.get().providers().placeholders().replace(player, this.group);
        AbstractMenusApi.get().providers().permissions().addGroup(player, group);
    }

    public static class Serializer implements NodeSerializer<ActionGroupAdd> {

        @Override
        public ActionGroupAdd deserialize(Class type, ConfigNode node) {
            return new ActionGroupAdd(node.getString());
        }

    }
}
