package ru.abstractmenus.data.actions.varp;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.variables.VarData;
import ru.abstractmenus.variables.VariableManagerImpl;

import java.util.List;

public class ActionVarpRem implements Action {

    private final List<VarData> dataList;

    private ActionVarpRem(List<VarData> dataList){
        this.dataList = dataList;
    }

    public void activate(Player p, Menu menu, Item clickedItem){
        for (VarData data : dataList) {
            String varName = Handlers.getPlaceholderHandler().replace(p, data.getName());

            VariableManagerImpl.instance().deletePersonal(p.getName(), varName);
        }
    }

    public static class Serializer implements NodeSerializer<ActionVarpRem> {

        @Override
        public ActionVarpRem deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionVarpRem(node.getList(VarData.class));
        }

    }

}
