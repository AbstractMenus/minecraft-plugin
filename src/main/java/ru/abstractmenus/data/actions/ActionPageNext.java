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

public class ActionPageNext implements Action {

    private final TypeInt skip;

    private ActionPageNext(TypeInt skip){
        this.skip = skip;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if (menu instanceof GeneratedMenu) {
            GeneratedMenu tmpl = (GeneratedMenu) menu;
            int skip = this.skip.getInt(player, menu);
            tmpl.nextPage(player, skip);
        }
    }

    public static class Serializer implements NodeSerializer<ActionPageNext> {

        @Override
        public ActionPageNext deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionPageNext(node.getValue(TypeInt.class));
        }
    }
}
