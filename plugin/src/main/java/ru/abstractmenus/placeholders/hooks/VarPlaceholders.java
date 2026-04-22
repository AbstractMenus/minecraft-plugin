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
            int colonIdx = placeholder.indexOf(':');
            if (colonIdx == -1 || colonIdx >= placeholder.length() - 1) return null;

            String rest = placeholder.substring(colonIdx + 1);
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

            int colonIdx = placeholder.indexOf(':');
            if (colonIdx == -1 || colonIdx >= placeholder.length() - 1) return null;

            String varName = placeholder.substring(colonIdx + 1);
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
            int colonIdx = placeholder.indexOf(':');
            if (colonIdx == -1 || colonIdx >= placeholder.length() - 1) return null;

            return getVarTime(VariableManagerImpl.instance().getGlobal(
                    placeholder.substring(colonIdx + 1)));
        }
    }

    public static class VarTempPlayerHook implements PlaceholderHook {

        @Override
        public String replace(String placeholder, Player player) {
            if (player == null) return null;

            int colonIdx = placeholder.indexOf(':');
            if (colonIdx == -1 || colonIdx >= placeholder.length() - 1) return null;

            return getVarTime(VariableManagerImpl.instance().getPersonal(
                    player.getName(), placeholder.substring(colonIdx + 1)));
        }
    }

    private static String getVarTime(Var var) {
        return TimeUtil.getTimeString((var != null) ? var.expiry() - System.currentTimeMillis() : 0L);
    }

}
