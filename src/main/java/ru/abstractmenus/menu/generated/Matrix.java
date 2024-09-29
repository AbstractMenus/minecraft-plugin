package ru.abstractmenus.menu.generated;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.api.inventory.Item;

import java.util.*;

public class Matrix {

    private final Map<Integer, Item> templates;

    public Matrix(Map<Integer, Item> templates){
        this.templates = templates;
    }

    public Item getItem(int slot){
        return templates.get(slot);
    }

    public Collection<Integer> getSlots(){
        return templates.keySet();
    }

    public static class Serializer implements NodeSerializer<Matrix>{

        @Override
        public Matrix deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            List<String> cells = node.node("cells").getList(String.class);
            Map<String, ConfigNode> map = node.node("templates").childrenMap();
            Map<Character, Item> templates = new HashMap<>();
            Map<Integer, Item> items = new LinkedHashMap<>();

            for (Map.Entry<String, ConfigNode> entry : map.entrySet()) {
                char key = entry.getKey().charAt(0);
                Item item = entry.getValue().getValue(Item.class);
                templates.put(key, item);
            }

            int slot = 0;

            for (String row : cells) {
                char[] chars = row.toCharArray();

                for (char c : chars) {
                    Item item = templates.get(c);

                    if (item != null) {
                        items.put(slot, item);
                    }

                    slot++;
                }
            }

            return new Matrix(items);
        }

    }
}
