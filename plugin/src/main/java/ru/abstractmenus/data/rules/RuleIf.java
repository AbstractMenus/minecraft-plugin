package ru.abstractmenus.data.rules;

import ru.abstractmenus.data.comparator.ModernValueComparator;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.data.comparator.LegacyValueComparator;
import ru.abstractmenus.data.comparator.ValueComparator;

public class RuleIf implements Rule {

    private final ValueComparator comparator;

    private RuleIf(ValueComparator comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        try {
            return comparator.compare(player, menu);
        } catch (Throwable t) {
            Logger.severe("Cannot execute 'if' rule: " + t.getMessage());
        }
        return false;
    }

    public static class Serializer implements NodeSerializer<RuleIf> {

        @Override
        public RuleIf deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            ValueComparator comparator;

            if (node.isPrimitive()) {
                comparator = node.getValue(ModernValueComparator.class);
            } else {
                comparator = node.getValue(LegacyValueComparator.class);
            }

            return new RuleIf(comparator);
        }

    }
}
