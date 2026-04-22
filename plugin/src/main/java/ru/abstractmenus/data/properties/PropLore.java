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

import java.util.List;

public class PropLore implements ItemProperty {

    private final List<String> lore;
    /**
     * Pre-computed legacy-formatted lore for fully-static inputs (no line contains
     * a placeholder). When non-null, {@link #apply} skips the per-render
     * placeholder-replace + MiniMessage round trip across every line and assigns
     * the cached list directly. Most production menus have predominantly static
     * lore — for them this drops dozens of MiniMessage parses per refresh to zero.
     */
    private final List<String> preFormatted;

    private PropLore(List<String> lore) {
        this.lore = lore;
        this.preFormatted = canPrecompute(lore) ? safePrecompute(lore) : null;
    }

    private static boolean canPrecompute(List<String> lines) {
        if (lines == null || lines.isEmpty()) return false;
        for (String line : lines) {
            if (line != null && DataType.hasPlaceholder(line)) return false;
        }
        return true;
    }

    private static List<String> safePrecompute(List<String> lines) {
        try {
            // MiniMessageUtil.parseToLegacy(List) returns a fresh list; copy it
            // immutable so we can hand the same reference to setLore on every
            // render without worrying about callers mutating it.
            return List.copyOf(MiniMessageUtil.parseToLegacy(lines));
        } catch (Throwable ignore) {
            // MiniMessageUtil might not be initialised yet (e.g. unit tests).
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
            meta.setLore(preFormatted);
            return;
        }
        List<String> replaced = Handlers.getPlaceholderHandler().replace(player, lore);
        meta.setLore(MiniMessageUtil.parseToLegacy(replaced));
    }

    public static class Serializer implements NodeSerializer<PropLore> {

        @Override
        public PropLore deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropLore(Colors.ofList(node.getList(String.class)));
        }

    }
}
