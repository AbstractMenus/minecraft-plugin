package ru.abstractmenus.data.comparator;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.Handlers;

public final class ModernValueComparator implements ValueComparator {

    private static final BooleanEvaluator EVALUATOR = new BooleanEvaluator();
    private final String expression;

    private ModernValueComparator(String expression) {
        this.expression = expression;
    }

    @Override
    public boolean compare(Player player, Menu menu) {
        String replaced = Handlers.getPlaceholderHandler().replace(player, expression);
        String result = EVALUATOR.evaluate(replaced);
        return Boolean.parseBoolean(result);
    }

    public static class Serializer implements NodeSerializer<ModernValueComparator> {

        @Override
        public ModernValueComparator deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ModernValueComparator(node.getString());
        }
    }
}
