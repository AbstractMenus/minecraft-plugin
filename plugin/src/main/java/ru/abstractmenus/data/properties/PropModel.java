package ru.abstractmenus.data.properties;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.datatype.DataType;
import ru.abstractmenus.datatype.TypeBool;
import ru.abstractmenus.datatype.TypeColor;
import ru.abstractmenus.datatype.TypeFloat;
import ru.abstractmenus.datatype.TypeInt;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.ArrayList;
import java.util.List;

public class PropModel implements ItemProperty {

    private final TypeInt model;
    private final List<TypeFloat> floats;
    private final List<TypeBool> flags;
    private final List<String> strings;
    private final List<TypeColor> colors;

    private PropModel(TypeInt model, List<TypeFloat> floats, List<TypeBool> flags, List<String> strings, List<TypeColor> colors) {
        this.model = model;
        this.floats = floats;
        this.flags = flags;
        this.strings = strings;
        this.colors = colors;
    }

    @Override
    public boolean canReplaceMaterial() {
        return false;
    }

    @Override
    public boolean isApplyMeta() {
        return true;
    }

    @Override
    public void apply(ItemStack itemStack, ItemMeta meta, Player player, Menu menu) {
        CustomModelDataComponent component = meta.getCustomModelDataComponent();
        component.setFloats(model != null ? List.of((float) model.getInt(player, menu)) : floats.stream().map(value -> value.getFloat(player, menu)).toList());
        component.setFlags(flags.stream().map(value -> value.getBool(player, menu)).toList());
        component.setStrings(strings.stream().map(value -> DataType.hasPlaceholder(value) ? AbstractMenusApi.get().providers().placeholders().resolve().replace(player, value) : value).toList());
        component.setColors(colors.stream().map(value -> value.getColor(player, menu)).toList());
        meta.setCustomModelDataComponent(component);
    }

    public static class Serializer implements NodeSerializer<PropModel> {

        @Override
        public PropModel deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if (node.isPrimitive()) {
                return new PropModel(new TypeInt.Serializer().deserialize(TypeInt.class, node), List.of(), List.of(), List.of(), List.of());
            }
            if (node.isList() || node.rawValue() == null) {
                throw new NodeSerializeException(node, "model must be an integer or an object");
            }
            return new PropModel(null,
                    readList(node.node("floats"), TypeFloat.class, new TypeFloat.Serializer()),
                    readList(node.node("flags"), TypeBool.class, new TypeBool.Serializer()),
                    readList(node.node("strings"), String.class, (stringType, entry) -> entry.getString()),
                    readList(node.node("colors"), TypeColor.class, new TypeColor.Serializer()));
        }

        private <T> List<T> readList(ConfigNode node, Class<T> type, NodeSerializer<T> serializer) throws NodeSerializeException {
            if (node.rawValue() == null) {
                return List.of();
            }
            if (!node.isList()) {
                throw new NodeSerializeException(node, "Model component field must be a list");
            }
            List<T> values = new ArrayList<>();
            for (ConfigNode entry : node.childrenList()) {
                values.add(serializer.deserialize(type, entry));
            }
            return values;
        }
    }
}
