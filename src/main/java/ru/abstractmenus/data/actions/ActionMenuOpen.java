package ru.abstractmenus.data.actions;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.services.MenuManager;

public class ActionMenuOpen implements Action {

    private final String menu;

    private ActionMenuOpen(String menu){
        this.menu = menu;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        String name = Handlers.getPlaceholderHandler().replace(player, this.menu);
        Menu menuToOpen = MenuManager.instance().getMenu(name);

        if(menuToOpen != null)
            MenuManager.instance().openMenu(player, menuToOpen);
    }

    public static class Serializer implements NodeSerializer<ActionMenuOpen> {

        @Override
        public ActionMenuOpen deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionMenuOpen(node.getString());
        }

    }
}
