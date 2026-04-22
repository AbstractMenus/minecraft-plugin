package ru.abstractmenus.data.rules.logical;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;

public class RuleNot implements Rule {

    private final Rule wrapped;

    public RuleNot(Rule wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        return !wrapped.check(player, menu, clickedItem);
    }
}