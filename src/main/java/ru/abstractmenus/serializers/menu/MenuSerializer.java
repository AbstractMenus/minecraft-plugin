package ru.abstractmenus.serializers.menu;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;

public interface MenuSerializer<T> {

    T deserialize(ConfigNode node, String title, int size) throws NodeSerializeException;

}
