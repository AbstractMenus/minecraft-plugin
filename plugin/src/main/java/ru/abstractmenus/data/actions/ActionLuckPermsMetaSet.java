package ru.abstractmenus.data.actions;


import lombok.Setter;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Handlers;
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
        metaList.forEach(meta -> {
            String replacedValue = isIgnorePlaceholder ? meta.getValue() : Handlers.getPlaceholderHandler().replace(player, meta.getValue());
            if (Handlers.getPermissionsHandler() instanceof LuckPermsHandler handler) {
                handler.addMeta(player, meta.getKey(), replacedValue);
            }
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
