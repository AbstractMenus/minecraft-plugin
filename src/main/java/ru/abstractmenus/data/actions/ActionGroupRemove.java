package ru.abstractmenus.data.actions;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;

public class ActionGroupRemove implements Action {

    private final String group;

    private ActionGroupRemove(String group){
        this.group = group;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        String groupName = Handlers.getPlaceholderHandler().replace(player, group);
        Handlers.getPermissionsHandler().removeGroup(player, groupName);
    }

    public static class Serializer implements NodeSerializer<ActionGroupRemove> {

        @Override
        public ActionGroupRemove deserialize(Class type, ConfigNode node) {
            return new ActionGroupRemove(node.getString());
        }

    }
}
