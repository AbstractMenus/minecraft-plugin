package ru.abstractmenus.placeholders.hooks;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.variables.Var;
import ru.abstractmenus.placeholders.PlaceholderHook;
import ru.abstractmenus.variables.VariableManagerImpl;
import ru.abstractmenus.util.TimeUtil;

public class VarPlaceholders {

    public static class VarHook implements PlaceholderHook {

        @Override
        public String replace(String placeholder, Player player) {
            String rest = parameter(placeholder);
            String defaultVal = "";
            int secondColon = rest.indexOf(':');
            if (secondColon != -1) {
                defaultVal = rest.substring(secondColon + 1);
                rest = rest.substring(0, secondColon);
            }

            int dotIdx = rest.indexOf('.');
            Var var;
            if (dotIdx == -1) {
                var = VariableManagerImpl.instance().getGlobal(rest);
            } else {
                var = VariableManagerImpl.instance().getPersonal(
                        rest.substring(0, dotIdx), rest.substring(dotIdx + 1));
            }

            return var != null ? var.value() : defaultVal;
        }

    }

    public static class VarPlayerHook implements PlaceholderHook {

        @Override
        public String replace(String placeholder, Player player) {
            if (player == null) return null;

            String varName = parameter(placeholder);
            String defaultVal = "";
            int secondColon = varName.indexOf(':');
            if (secondColon != -1) {
                defaultVal = varName.substring(secondColon + 1);
                varName = varName.substring(0, secondColon);
            }

            Var var = VariableManagerImpl.instance().getPersonal(player.getName(), varName);
            return var != null ? var.value() : defaultVal;
        }

    }

    public static class VarTempHook implements PlaceholderHook {

        @Override
        public String replace(String placeholder, Player player) {
            String[] parts = parameter(placeholder).split(":", 2);
            Var var = VariableManagerImpl.instance().getGlobal(parts[0]);
            return var == null && parts.length > 1 ? parts[1] : getVarTime(var);
        }
    }

    public static class VarTempPlayerHook implements PlaceholderHook {

        @Override
        public String replace(String placeholder, Player player) {
            if (player == null) return null;

            String[] parts = parameter(placeholder).split(":", 2);
            Var var = VariableManagerImpl.instance().getPersonal(player.getName(), parts[0]);
            return var == null && parts.length > 1 ? parts[1] : getVarTime(var);
        }
    }

    private static String parameter(String placeholder) {
        if (placeholder.startsWith(":")) return placeholder.substring(1);
        if (placeholder.startsWith("get:")) return placeholder.substring(4);
        return placeholder;
    }

    private static String getVarTime(Var var) {
        return TimeUtil.getTimeString((var != null) ? var.expiry() - System.currentTimeMillis() : 0L);
    }

}
