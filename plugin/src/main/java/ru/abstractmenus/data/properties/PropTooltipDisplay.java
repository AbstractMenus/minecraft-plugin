package ru.abstractmenus.data.properties;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.datatype.TypeBool;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class PropTooltipDisplay implements ItemProperty {

    private final TypeBool hideTooltip;
    private final Set<DataComponentType> hiddenComponents;

    private PropTooltipDisplay(TypeBool hideTooltip, Set<DataComponentType> hiddenComponents) {
        this.hideTooltip = hideTooltip;
        this.hiddenComponents = hiddenComponents;
    }

    @Override
    public boolean canReplaceMaterial() {
        return false;
    }

    @Override
    public boolean isApplyMeta() {
        return false;
    }

    @Override
    public void apply(ItemStack itemStack, ItemMeta meta, Player player, Menu menu) {
        if (hideTooltip == null && hiddenComponents == null) {
            return;
        }

        tooltipDisplay(itemStack, player, menu);
    }

    private void tooltipDisplay(ItemStack itemStack, Player player, Menu menu) {
        TooltipDisplay existing = itemStack.getData(DataComponentTypes.TOOLTIP_DISPLAY);

        boolean hide = (hideTooltip != null)
                ? hideTooltip.getBool(player, menu)
                : (existing != null && existing.hideTooltip());

        Set<DataComponentType> hidden = new LinkedHashSet<>();
        if (existing != null) {
            hidden.addAll(existing.hiddenComponents());
        }
        if (hiddenComponents != null) {
            hidden.addAll(hiddenComponents);
        }

        itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                .hideTooltip(hide)
                .hiddenComponents(hidden)
                .build());
    }

    public static class Serializer implements NodeSerializer<PropTooltipDisplay> {

        @Override
        public PropTooltipDisplay deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            TypeBool hideTooltip = null;
            Set<DataComponentType> hidden = null;

            ConfigNode hideNode = node.node("hideTooltip");
            if (hideNode.rawValue() != null) {
                hideTooltip = new TypeBool.Serializer().deserialize(TypeBool.class, hideNode);
            }

            ConfigNode hiddenNode = node.node("hiddenComponents");
            if (hiddenNode.rawValue() != null) {
                if (!hiddenNode.isList()) {
                    throw new NodeSerializeException(node, "Field 'hiddenComponents' must be a list");
                }
                hidden = new LinkedHashSet<>();
                for (ConfigNode componentNode : hiddenNode.childrenList()) {
                    String raw = componentNode.getString();
                    DataComponentType component = resolveComponent(raw);
                    if (component == null) {
                        throw new NodeSerializeException(componentNode, "Unknown data component '" + raw + "'");
                    }
                    hidden.add(component);
                }
            }

            return new PropTooltipDisplay(hideTooltip, hidden);
        }

    }

    private static DataComponentType resolveComponent(String raw) {
        if (raw == null) {
            return null;
        }
        NamespacedKey key = NamespacedKey.fromString(raw.trim().toLowerCase(Locale.ROOT));
        return key == null ? null : Registry.DATA_COMPONENT_TYPE.get(key);
    }
}
