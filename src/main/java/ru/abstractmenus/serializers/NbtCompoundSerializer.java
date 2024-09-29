package ru.abstractmenus.serializers;

import de.tr7zw.nbtapi.*;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.hocon.api.serialize.NodeSerializers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NbtCompoundSerializer implements NodeSerializer<NBTCompound> {

    public NBTCompound deserialize(Class<NBTCompound> type, ConfigNode node) throws NodeSerializeException {
        return getCompound(node);
    }

    private Object getElement(ConfigNode node){
        if (node.isPrimitive()) {
            return node.rawValue();
        } else if (node.isMap()) {
            return getCompound(node);
        } else if (node.isList()) {
            return getList(node);
        } else {
            return null;
        }
    }

    private List<Object> getList(ConfigNode node){
        List<? extends ConfigNode> children = node.childrenList();
        List<Object> list = new ArrayList<>();

        for (ConfigNode elem : children){
            list.add(getElement(elem));
        }

        return list;
    }

    private NBTCompound getCompound(ConfigNode node){
        Map<String, ConfigNode> children = node.childrenMap();
        NBTContainer compound = new NBTContainer();

        for (Map.Entry<String, ConfigNode> entry : children.entrySet()){
            String key = entry.getKey();
            Object elem = getElement(entry.getValue());
            putValue(compound, key, elem);
        }

        return compound;
    }

    private void putValue(NBTCompound compound, String key, Object value){
        if (value instanceof NBTCompound){
            compound.getOrCreateCompound(key).mergeCompound((NBTCompound) value);
        } else if (value instanceof List){
            putList(compound, key, (List) value);
        } else if (value instanceof String){
            compound.setString(key, parseStr(value));
        } else if (value instanceof Boolean){
            compound.setBoolean(key, (Boolean) value);
        } else if (value instanceof Byte){
            compound.setByte(key, (Byte) value);
        } else if (value instanceof Integer){
            compound.setInteger(key, (Integer) value);
        } else if (value instanceof Double){
            compound.setDouble(key, (Double) value);
        } else if (value instanceof Float){
            compound.setFloat(key, (Float) value);
        } else if (value instanceof Long){
            compound.setLong(key, (Long) value);
        } else if (value instanceof Short){
            compound.setShort(key, (Short) value);
        } else if (value instanceof UUID){
            compound.setUUID(key, (UUID) value);
        } else {
            compound.setObject(key, value);
        }
    }

    private void putList(NBTCompound compound, String key, List value){
        if (value.isEmpty()) return;

        Object sample = value.get(0);

        if (sample instanceof String){
            compound.getStringList(key).addAll(parseStrList(value));
        } else if (sample instanceof Integer){
            compound.getIntegerList(key).addAll(value);
        } else if (sample instanceof Long){
            compound.getLongList(key).addAll(value);
        } else if (sample instanceof Double){
            compound.getDoubleList(key).addAll(value);
        } else if (sample instanceof Float){
            compound.getFloatList(key).addAll(value);
        } else if (sample instanceof NBTCompound){
            NBTCompoundList list = compound.getCompoundList(key);

            for (Object obj : value) {
                list.addCompound((NBTCompound) obj);
            }
        }
    }

    private String parseStr(Object obj){
        return Colors.of(obj.toString());
    }

    private List<String> parseStrList(List<String> list){
        return Colors.ofList(list);
    }

    public static void register(NodeSerializers serializers) {
        serializers.register(NBTCompound.class, new NbtCompoundSerializer());
    }
}
