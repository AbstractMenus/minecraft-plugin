package ru.abstractmenus.menu.item;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.data.Actions;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.util.TimeUtil;

import java.util.Map;

public class MenuItem extends InventoryItem {

    private Map<ClickType, Actions> clicks;
    private Actions anyClickActions;
    private Rule showRules;
    private Rule minorRules;

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

            if(actions != null) {
                actions.activate(clicker, menu, this);
            }
        }
    }

    public boolean checkShowRules(Player player, Menu menu){
        if (minorRules != null)
            minorRules.check(player, menu, this);

        return showRules == null || showRules.check(player, menu, this);
    }

    public void setClicks(Map<ClickType, Actions> data){
        this.clicks = data;
    }

    public void setShowRules(Rule rules){
        this.showRules = rules;
    }

    public void setMinorRules(Rule rules){
        this.minorRules = rules;
    }

    public void setClickCooldown(int clickCooldown) {
        this.clickCooldown = clickCooldown;
    }

    public void setAnyClickActions(Actions actions){
        this.anyClickActions = actions;
    }

    @Override
    public MenuItem clone() {
        return (MenuItem) super.clone();
    }
}
