package ru.abstractmenus.data.properties;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Item property controlling the {@code tooltip_display} data component
 * ({@link TooltipDisplay}). Hides a tooltip entirely via {@code hideTooltip}
 * or hides the tooltip of specific components via {@code hiddenComponents}.
 *
 * <pre>{@code
 * tooltipDisplay {
 *     hideTooltip: false
 *     hiddenComponents: [BUNDLE_CONTENTS, ENCHANTMENTS]
 * }
 * }</pre>
 */
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
        return true;
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

        Set<DataComponentType> hidden = (hiddenComponents != null)
                ? hiddenComponents
                : (existing != null ? existing.hiddenComponents() : Collections.emptySet());

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

    /**
     * Test hook: replaces the live {@link Registry#DATA_COMPONENT_TYPE} source.
     * Accepts any {@link Iterable} so tests do not need to mock the Registry
     * class itself (its static initializer requires a live Paper server).
     */
    static void configureRegistry(Iterable<DataComponentType> registry) {
        registryOverride = registry;
        cache = null;
    }

    /**
     * Test hook: restores the live registry source and clears the cache.
     */
    static void resetRegistry() {
        registryOverride = null;
        cache = null;
    }

    static DataComponentType resolveComponent(String raw) {
        if (raw == null) {
            return null;
        }
        String key = raw.trim().toLowerCase(Locale.ROOT);
        int colon = key.indexOf(':');
        if (colon >= 0) {
            key = key.substring(colon + 1);
        }
        if (key.isEmpty()) {
            return null;
        }
        return registryMap().get(key);
    }

    private static volatile Iterable<DataComponentType> registryOverride;
    private static volatile Map<String, DataComponentType> cache;

    private static Map<String, DataComponentType> registryMap() {
        Map<String, DataComponentType> local = cache;
        if (local != null) {
            return local;
        }
        Iterable<DataComponentType> source = registryOverride;
        if (source == null) {
            try {
                source = Registry.DATA_COMPONENT_TYPE;
            } catch (Throwable t) {
                source = null;
            }
        }
        Map<String, DataComponentType> built = new HashMap<>();
        if (source != null) {
            for (DataComponentType type : source) {
                if (type.getKey() == null) {
                    continue;
                }
                built.put(type.getKey().getKey().toLowerCase(Locale.ROOT), type);
            }
        }
        cache = built;
        return built;
    }
}
