package ru.abstractmenus.serializers.menu;

import ru.abstractmenus.api.Catalog;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.menu.generated.GeneratedMenu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Types;
import ru.abstractmenus.menu.generated.Matrix;

public class GeneratedMenuSerializer implements MenuSerializer<GeneratedMenu> {

    @Override
    public GeneratedMenu deserialize(ConfigNode node, String title, int size) throws NodeSerializeException {
        GeneratedMenu menu = new GeneratedMenu(title, size);

        String catalogType = node.node("catalog", "type").getString();
        Class<? extends Catalog<?>> catalogToken = Types.getCatalogType(catalogType);

        if (catalogToken != null) {
            menu.setCatalog(node.node("catalog").getValue(catalogToken));
        } else {
            throw new NodeSerializeException(node.node("catalog"), "Catalog with type '"+catalogType+"' not found");
        }

        if (node.node("matrix").rawValue() != null){
            menu.setMatrix(node.node("matrix").getValue(Matrix.class));
        }

        if(node.node("items").rawValue() != null){
            menu.setItems(node.node("items").getList(Item.class));
        }

        return menu;
    }
}
