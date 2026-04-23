package ru.abstractmenus.data.actions.var;

import ru.abstractmenus.api.variables.Var;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.util.TimeUtil;
import ru.abstractmenus.variables.VarData;
import ru.abstractmenus.variables.VariableManagerImpl;

import java.util.List;

public class ActionVarSet implements Action {

    private final List<VarData> dataList;

    private ActionVarSet(List<VarData> dataList) {
        this.dataList = dataList;
    }

    public void activate(Player p, Menu menu, Item clickedItem) {
        for (VarData data : dataList) {
            String varName = AbstractMenusApi.get().providers().placeholders().replace(p, data.getName());
            String varVal = AbstractMenusApi.get().providers().placeholders().replace(p, data.getValue());

            long time = TimeUtil.parseTime(AbstractMenusApi.get().providers().placeholders().replace(p, data.getTime()));
            boolean replace = data.isReplace().getBool(p, menu);

            Var var = VariableManagerImpl.instance().createBuilder()
                    .name(varName)
                    .value(varVal)
                    .expiry((time > 0L) ? System.currentTimeMillis() + time : 0L)
                    .build();

            if (data.getPlayer() == null) {
                VariableManagerImpl.instance().saveGlobal(var, replace);
            } else {
                String playerName = AbstractMenusApi.get().providers().placeholders().replace(p, data.getPlayer());
                VariableManagerImpl.instance().savePersonal(playerName, var, replace);
            }
        }
    }

    public static class Serializer implements NodeSerializer<ActionVarSet> {

        @Override
        public ActionVarSet deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionVarSet(node.getList(VarData.class));
        }

    }

}
