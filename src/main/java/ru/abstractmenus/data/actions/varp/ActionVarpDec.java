package ru.abstractmenus.data.actions.varp;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.variables.VarNumData;
import ru.abstractmenus.variables.VariableManagerImpl;

import java.util.List;
import java.util.function.Function;

public class ActionVarpDec implements Action {

    private final List<VarNumData> dataList;

    private ActionVarpDec(List<VarNumData> dataList) {
        this.dataList = dataList;
    }

    public void activate(Player p, Menu menu, Item clickedItem) {
        for (VarNumData data : dataList) {
            String varName = Handlers.getPlaceholderHandler().replace(p, data.getName());
            double value = data.getValue().getDouble(p, menu);
            Function<Double, Double> func = num -> num - value;

            VariableManagerImpl.instance().modifyNumericPersonal(p.getName(), varName, func);
        }
    }

    public static class Serializer implements NodeSerializer<ActionVarpDec> {

        @Override
        public ActionVarpDec deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionVarpDec(node.getList(VarNumData.class));
        }

    }

}
