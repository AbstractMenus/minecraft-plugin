package ru.abstractmenus.data.actions.var;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.variables.VarData;
import ru.abstractmenus.variables.VariableManagerImpl;

import java.util.List;

public class ActionVarRem implements Action {

    private final List<VarData> dataList;

    private ActionVarRem(List<VarData> dataList){
        this.dataList = dataList;
    }

    public void activate(Player p, Menu menu, Item clickedItem){
        for (VarData data : dataList) {
            String varName = Handlers.getPlaceholderHandler().replace(p, data.getName());

            if(data.getPlayer() == null) {
                VariableManagerImpl.instance().deleteGlobal(varName);
            } else {
                String playerName = Handlers.getPlaceholderHandler().replace(p, data.getPlayer());
                VariableManagerImpl.instance().deletePersonal(playerName, varName);
            }
        }
    }

    public static class Serializer implements NodeSerializer<ActionVarRem> {

        @Override
        public ActionVarRem deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionVarRem(node.getList(VarData.class));
        }

    }

}
