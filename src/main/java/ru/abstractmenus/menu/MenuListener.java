package ru.abstractmenus.menu;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;

public interface MenuListener {

    void onOpen(Player player, Menu menu);

}
