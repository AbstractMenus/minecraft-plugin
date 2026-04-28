package ru.abstractmenus.data.actions;


import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.handler.PermissionsHandler;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.handlers.LuckPermsHandler;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.List;

@RequiredArgsConstructor
public class ActionLuckPermsMetaRemove implements Action {

    private final List<String> metaList;

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        PermissionsHandler perms = AbstractMenusApi.get().providers().permissions().resolve();
        if (!(perms instanceof LuckPermsHandler handler)) {
            Logger.warning("lpMetaRemove skipped: active permissions provider "
                    + (perms == null ? "null" : perms.getClass().getSimpleName())
                    + " is not LuckPerms. "
                    + "Install LuckPerms or set 'providers.permissions = \"luckperms\"' in config.conf.");
            return;
        }
        metaList.forEach(metaKey -> handler.removeMeta(player, metaKey));
    }

    public static class Serializer implements NodeSerializer<ActionLuckPermsMetaRemove> {

        @Override
        public ActionLuckPermsMetaRemove deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionLuckPermsMetaRemove(node.getList(String.class));
        }

    }
}
