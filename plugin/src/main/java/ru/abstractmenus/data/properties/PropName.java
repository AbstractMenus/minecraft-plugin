package ru.abstractmenus.data.properties;

import ru.abstractmenus.datatype.DataType;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.util.MiniMessageUtil;

public class PropName implements ItemProperty {

    private final String name;
    /**
     * Pre-computed legacy-formatted display name for static inputs (no placeholders).
     * When non-null, {@link #apply} skips the placeholder-replace + MiniMessage round
     * trip entirely and assigns the cached string directly. Most production menus
     * have predominantly static names, so this is the largest single saving on the
     * MiniMessage hot path after the appendReplacement rewrite.
     */
    private final String preFormatted;

    private PropName(String name) {
        this.name = name;
        this.preFormatted = (name != null && !DataType.hasPlaceholder(name))
                ? safePrecompute(name)
                : null;
    }

    private static String safePrecompute(String input) {
        try {
            return MiniMessageUtil.parseToLegacy(input);
        } catch (Throwable ignore) {
            // MiniMessageUtil might not be initialised yet (e.g. unit tests) —
            // fall back to per-call resolution rather than failing load.
            return null;
        }
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
        if (preFormatted != null) {
            meta.setDisplayName(preFormatted);
            return;
        }
        String replaced = Handlers.getPlaceholderHandler().replace(player, name);
        meta.setDisplayName(MiniMessageUtil.parseToLegacy(replaced));
    }

    public static class Serializer implements NodeSerializer<PropName> {

        @Override
        public PropName deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropName(Colors.of(node.getString()));
        }

    }
}
