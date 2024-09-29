package ru.abstractmenus.datatype;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Slot;
import ru.abstractmenus.api.inventory.slot.SlotIndex;
import ru.abstractmenus.api.inventory.slot.SlotMatrix;
import ru.abstractmenus.api.inventory.slot.SlotPos;
import ru.abstractmenus.api.inventory.slot.SlotRange;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeSlot extends DataType {

    private Slot slot;

    public TypeSlot(String value) {
        super(value);
    }

    public TypeSlot(Slot slot) {
        super(null);
        this.slot = slot;
    }

    public Slot getSlot(Player player, Menu menu) {
        return slot != null ? slot : Serializer.parseStr(replaceFor(player, menu));
    }

    public static class Serializer implements NodeSerializer<TypeSlot> {

        private static final Pattern POS_PATTERN = Pattern.compile("^(\\d+)\\s*,\\s*(\\d+)$");
        private static final Pattern RANGE_PATTERN = Pattern.compile("^(\\d+)\\s*-\\s*(\\d+)$");

        @Override
        public TypeSlot deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if (node.isPrimitive()) {
                int index = node.getInt(Integer.MIN_VALUE);

                if (index != Integer.MIN_VALUE) {
                    return new TypeSlot(new SlotIndex(index));
                }

                String str = node.getString();

                if (hasPlaceholder(str)) {
                    return new TypeSlot(str);
                }

                return new TypeSlot(parseStr(str));
            } else if (node.isList()) {
                return new TypeSlot(parseMatrix(node.getList(String.class)));
            }

            throw new NodeSerializeException(node, "Undefined slot format");
        }

        // slot: "x,y" - SlotPos
        // slot: "from-to" - SlotRange
        public static Slot parseStr(String str) {
            try {
                int slotIndex = Integer.parseInt(str);
                return new SlotIndex(slotIndex);
            } catch (Exception ex) {
                // Ignore
            }

            Matcher matcher = POS_PATTERN.matcher(str);

            if (matcher.matches()) {
                int x = Integer.parseInt(matcher.group(1), 10);
                int y = Integer.parseInt(matcher.group(2), 10);
                return new SlotPos(x, y);
            }

            matcher = RANGE_PATTERN.matcher(str);

            if (matcher.matches()) {
                int from = Integer.parseInt(matcher.group(1), 10);
                int to = Integer.parseInt(matcher.group(2), 10);
                return new SlotRange(from, to);
            }

            throw new RuntimeException(String.format("Cannot parse slot line. Undefined format: '%s'", str));
        }

        private static Slot parseMatrix(List<String> rows) {
            List<Integer> slots = new ArrayList<>();
            int rowNum = 0;

            for (String row : rows) {
                int slot = rowNum * 9;

                for (char cell : row.toCharArray()) {
                    if (cell != '-') {
                        slots.add(slot);
                    }
                    slot++;
                }

                rowNum++;
            }

            return new SlotMatrix(slots.toArray(new Integer[0]));
        }
    }

}
