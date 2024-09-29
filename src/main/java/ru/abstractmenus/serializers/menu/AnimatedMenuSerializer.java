package ru.abstractmenus.serializers.menu;

import ru.abstractmenus.data.Actions;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.menu.animated.AnimatedMenu;
import ru.abstractmenus.menu.animated.Frame;

public class AnimatedMenuSerializer implements MenuSerializer<AnimatedMenu> {

    @Override
    public AnimatedMenu deserialize(ConfigNode node, String title, int size) throws NodeSerializeException {
        AnimatedMenu menu = new AnimatedMenu(title, size);

        if(node.node("onAnimStart").rawValue() != null){
            menu.setAnimStartActions(node.node("onAnimStart").getValue(Actions.class));
        }

        if(node.node("onAnimEnd").rawValue() != null){
            menu.setAnimEndActions(node.node("onAnimEnd").getValue(Actions.class));
        }

        menu.setLoop(node.node("loop").getBoolean(false));
        menu.setFrames(node.node("frames").getList(Frame.class));

        if(node.node("items").rawValue() != null){
            menu.setItems(node.node("items").getList(Item.class));
        }

        return menu;
    }
}
