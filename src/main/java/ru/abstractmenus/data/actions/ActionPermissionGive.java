package ru.abstractmenus.data.actions;


import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.List;

public class ActionPermissionGive implements Action {

    private boolean isIgnorePlaceholder = false;

    private List<String> permissions;

    public void setIgnorePlaceholder(boolean ignorePlaceholder) {
        isIgnorePlaceholder = ignorePlaceholder;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        permissions.forEach(perm -> {
            String replaced = isIgnorePlaceholder ? perm : Handlers.getPlaceholderHandler().replace(player, perm);
            Handlers.getPermissionsHandler().addPermission(player, replaced);
        });
    }

    public static class Serializer implements NodeSerializer<ActionPermissionGive> {

        @Override
        public ActionPermissionGive deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            ActionPermissionGive action = new ActionPermissionGive();

            if (node.node("ignorePlaceholder").rawValue() != null) {
                action.setIgnorePlaceholder(node.node("ignorePlaceholder").getBoolean());
            }

            action.setPermissions(node.getList(String.class));

            return action;
        }

    }
}
