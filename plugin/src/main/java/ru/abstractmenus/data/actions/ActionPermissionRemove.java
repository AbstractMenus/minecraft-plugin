package ru.abstractmenus.data.actions;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.AbstractMenusApi;

import java.util.List;

public class ActionPermissionRemove implements Action {

    private final List<String> permissions;

    private ActionPermissionRemove(List<String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        permissions.forEach(perm -> {
            String replaced = AbstractMenusApi.get().providers().placeholders().resolve().replace(player, perm);
            AbstractMenusApi.get().providers().permissions().resolve().removePermission(player, replaced);
        });
    }

    public static class Serializer implements NodeSerializer<ActionPermissionRemove> {

        @Override
        public ActionPermissionRemove deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionPermissionRemove(node.getList(String.class));
        }

    }
}
