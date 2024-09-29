package ru.abstractmenus.serializers.menu;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.menu.SimpleMenu;
import ru.abstractmenus.api.inventory.Item;

public class SimpleMenuSerializer implements MenuSerializer<SimpleMenu> {

    @Override
    public SimpleMenu deserialize(ConfigNode node, String title, int size) throws NodeSerializeException {
        SimpleMenu menu = new SimpleMenu(title, size);

        if(node.node("items").rawValue() != null){
            menu.setItems(node.node("items").getList(Item.class));
        }

        return menu;
    }
}
