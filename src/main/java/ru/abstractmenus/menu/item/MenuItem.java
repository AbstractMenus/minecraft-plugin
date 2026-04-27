package ru.abstractmenus.menu.item;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.data.Actions;
import ru.abstractmenus.api.inventory.Menu;

import java.util.Map;

public class MenuItem extends InventoryItem {

    @Setter
    private Map<ClickType, Actions> clicks;
    @Setter
    private Actions anyClickActions;
    @Setter
    private Rule showRules;
    @Setter
    private Rule minorRules;

    @Getter
    @Setter
    private int clickCooldown = 1;

    public void doClick(ClickType type, Menu menu, Player clicker) {
        // anyClickActions = the body of `click { ... }` without an explicit
        // ClickType key. Treat it as "any single click" - DOUBLE_CLICK is a
        // synthetic event Bukkit fires alongside a regular LEFT on rapid
        // clicks, so it must not retrigger anyClickActions. An explicit
        // `double_click { ... }` handler still runs via the `clicks` map.
        if (anyClickActions != null && type != ClickType.DOUBLE_CLICK)
            anyClickActions.activate(clicker, menu, this);

        if (clicks != null) {
            Actions actions = clicks.get(type);

            if (actions != null) {
                actions.activate(clicker, menu, this);
            }
        }
    }

    public boolean checkShowRules(Player player, Menu menu) {
        if (minorRules != null)
            minorRules.check(player, menu, this);

        return showRules == null || showRules.check(player, menu, this);
    }

    @Override
    public MenuItem clone() {
        return (MenuItem) super.clone();
    }
}
