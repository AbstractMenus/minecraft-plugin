package ru.abstractmenus.data.properties;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

@Setter
@Getter
public class PropLPMeta {

    private String key = StringUtils.EMPTY;
    private String value = StringUtils.EMPTY;

    public static class Serializer implements NodeSerializer<PropLPMeta> {

        @Override
        public PropLPMeta deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            PropLPMeta propLPMeta = new PropLPMeta();

            propLPMeta.setKey(node.node("key").getString());
            propLPMeta.setValue(node.node("value").getString());

            return propLPMeta;
        }

    }
}
