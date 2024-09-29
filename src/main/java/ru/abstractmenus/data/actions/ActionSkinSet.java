package ru.abstractmenus.data.actions;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;

public class ActionSkinSet implements Action {

    private final String texture;
    private final String signature;

    private ActionSkinSet(String texture, String signature){
        this.texture = texture;
        this.signature = signature;
    }

    public void activate(Player player, Menu menu, Item clickedItem){
        Handlers.getSkinHandler().setSkin(player,
                Handlers.getPlaceholderHandler().replace(player, texture),
                Handlers.getPlaceholderHandler().replace(player, signature));
    }

    public static class Serializer implements NodeSerializer<ActionSkinSet> {

        @Override
        public ActionSkinSet deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            String texture = node.node("texture").getString();
            String signature = node.node("signature").getString();
            return new ActionSkinSet(texture, signature);
        }

    }

}
