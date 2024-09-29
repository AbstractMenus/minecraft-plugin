package ru.abstractmenus.data.actions;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.services.MenuManager;

public class ActionMenuOpenCtx implements Action {

    private final String menu;

    private ActionMenuOpenCtx(String menu) {
        this.menu = menu;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        String name = Handlers.getPlaceholderHandler().replace(player, this.menu);
        Menu menuToOpen = MenuManager.instance().getMenu(name);

        if(menuToOpen != null) {
            Menu current = MenuManager.instance().getOpenedMenu(player);

            if (current == null) {
                Logger.severe("Cannot open menu with context. Previous menu not found. Maybe it destroyed");
                return;
            }

            Activator activator = current.getActivatedBy().orElse(null);
            Object ctx = current.getContext().orElse(null);

            MenuManager.instance().openMenu(activator, ctx, player, menuToOpen);
        }
    }

    public static class Serializer implements NodeSerializer<ActionMenuOpenCtx> {

        @Override
        public ActionMenuOpenCtx deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionMenuOpenCtx(node.getString());
        }

    }
}
