package ru.abstractmenus.data.actions;

import ru.abstractmenus.datatype.TypeInt;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.menu.generated.GeneratedMenu;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;

public class ActionPagePrev implements Action {

    private final TypeInt skip;

    private ActionPagePrev(TypeInt skip){
        this.skip = skip;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if (menu instanceof GeneratedMenu){
            GeneratedMenu tmpl = (GeneratedMenu) menu;
            int skip = this.skip.getInt(player, menu);
            tmpl.prevPage(player, skip);
        }
    }

    public static class Serializer implements NodeSerializer<ActionPagePrev> {

        @Override
        public ActionPagePrev deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionPagePrev(node.getValue(TypeInt.class));
        }

    }
}
