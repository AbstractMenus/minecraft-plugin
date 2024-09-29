package ru.abstractmenus.serializers;


import com.google.gson.*;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.List;
import java.util.Map;

public class JsonSerializer implements NodeSerializer<JsonElement> {

    private static final JsonParser JSON_PARSER = new JsonParser();

    public JsonElement deserialize(Class<JsonElement> type, ConfigNode node) throws NodeSerializeException {
        if (node.isPrimitive())
            return JSON_PARSER.parse(node.getString());

        return getElement(node);
    }

    private JsonElement getElement(ConfigNode node){
        JsonElement element = null;

        if (node.isPrimitive()) {
            element = getPrimitive(node);
        } else if (node.isMap()) {
            element = getObject(node);
        } else if (node.isList()) {
            element = getList(node);
        }

        return element;
    }

    private JsonPrimitive getPrimitive(ConfigNode node){
        if(node.rawValue() != null){
            String type = node.rawValue().getClass().getName();

            switch (type){
                default:
                    return new JsonPrimitive(node.getString("null"));
                case "java.lang.Integer":
                    return new JsonPrimitive(node.getInt());
                case "java.lang.Boolean":
                    return new JsonPrimitive(node.getBoolean());
                case "java.lang.Double":
                    return new JsonPrimitive(node.getDouble());
                case "java.lang.Float":
                    return new JsonPrimitive(node.getFloat());
            }
        }

        return new JsonPrimitive(node.getString("null"));
    }

    private JsonArray getList(ConfigNode node){
        List<? extends ConfigNode> list = node.childrenList();
        JsonArray arr = new JsonArray();

        for (ConfigNode elem : list){
            arr.add(getElement(elem));
        }

        return arr;
    }

    private JsonObject getObject(ConfigNode node){
        Map<String, ConfigNode> children = node.childrenMap();
        JsonObject object = new JsonObject();

        for (Map.Entry<String, ConfigNode> entry : children.entrySet()){
            String key = entry.getKey();
            object.add(key, getElement(entry.getValue()));
        }

        return object;
    }
}
