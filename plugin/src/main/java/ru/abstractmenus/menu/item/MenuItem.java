package ru.abstractmenus.menu.item;

import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.data.Actions;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.util.TimeUtil;

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

    @Setter
    private int clickCooldown = 1;
    private long cooldownExpiry;

    public void doClick(ClickType type, Menu menu, Player clicker) {
        if (clickCooldown > 0) {
            if (TimeUtil.currentTimeTicks() < cooldownExpiry)
                return;

            cooldownExpiry = TimeUtil.currentTimeTicks() + clickCooldown;
        }

        if (anyClickActions != null)
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
