package ru.abstractmenus.data.rules;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.api.Rule;

public abstract class RuleJS implements Rule {

    public static class Serializer implements NodeSerializer<RuleJS> {

        @Override
        public RuleJS deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            throw new NodeSerializeException(node, "The 'js' rule was removed because it allowed code injection. "
                    + "Use 'if' for comparisons and expressions, combined with 'and', 'or' or 'oneof' rules.");
        }
    }
}
