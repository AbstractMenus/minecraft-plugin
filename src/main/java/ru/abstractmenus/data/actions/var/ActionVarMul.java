package ru.abstractmenus.data.actions.var;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.variables.VarNumData;
import ru.abstractmenus.variables.VariableManagerImpl;

import java.util.List;
import java.util.function.Function;

public class ActionVarMul implements Action {

    private final List<VarNumData> dataList;

    private ActionVarMul(List<VarNumData> dataList) {
        this.dataList = dataList;
    }

    public void activate(Player p, Menu menu, Item clickedItem){
        for (VarNumData data : dataList){
            String varName = Handlers.getPlaceholderHandler().replace(p, data.getName());
            double value = data.getValue().getDouble(p, menu);
            Function<Double, Double> func = num -> num * value;

            if(data.getPlayer() == null) {
                VariableManagerImpl.instance().modifyNumericGlobal(varName, func);
            } else {
                String playerName = Handlers.getPlaceholderHandler().replace(p, data.getPlayer());
                VariableManagerImpl.instance().modifyNumericPersonal(playerName, varName, func);
            }
        }
    }

    public static class Serializer implements NodeSerializer<ActionVarMul> {

        @Override
        public ActionVarMul deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionVarMul(node.getList(VarNumData.class));
        }

    }

}
