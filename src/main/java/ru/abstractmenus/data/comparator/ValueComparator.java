package ru.abstractmenus.data.comparator;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;

public interface ValueComparator {

    boolean compare(Player player, Menu menu);

}
