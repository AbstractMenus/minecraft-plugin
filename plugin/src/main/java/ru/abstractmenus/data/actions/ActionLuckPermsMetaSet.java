package ru.abstractmenus.data.actions;


import lombok.Setter;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.handler.PermissionsHandler;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.data.properties.PropLPMeta;
import ru.abstractmenus.handlers.LuckPermsHandler;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.List;

@Setter
public class ActionLuckPermsMetaSet implements Action {

    private boolean isIgnorePlaceholder = false;
    private List<PropLPMeta> metaList;

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        PermissionsHandler perms = AbstractMenusApi.get().providers().permissions().resolve();
        if (!(perms instanceof LuckPermsHandler handler)) {
            Logger.warning("lpMetaSet skipped: active permissions provider "
                    + (perms == null ? "null" : perms.getClass().getSimpleName())
                    + " is not LuckPerms. "
                    + "Install LuckPerms or set 'providers.permissions = \"luckperms\"' in config.conf.");
            return;
        }
        metaList.forEach(meta -> {
            String replacedValue = isIgnorePlaceholder ? meta.getValue()
                    : AbstractMenusApi.get().providers().placeholders().resolve().replace(player, meta.getValue());
            handler.addMeta(player, meta.getKey(), replacedValue);
        });
    }

    public static class Serializer implements NodeSerializer<ActionLuckPermsMetaSet> {

        @Override
        public ActionLuckPermsMetaSet deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            ActionLuckPermsMetaSet action = new ActionLuckPermsMetaSet();

            if (node.node("ignorePlaceholder").rawValue() != null) {
                action.setIgnorePlaceholder(node.node("ignorePlaceholder").getBoolean());
            }

            action.setMetaList(node.node("metaList").getList(PropLPMeta.class));

            return action;
        }

    }
}
