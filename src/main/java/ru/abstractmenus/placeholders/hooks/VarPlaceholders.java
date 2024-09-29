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
            String[] arr = placeholder.split(":");

            if(arr.length > 1){
                String[] data = arr[1].split("\\.");
                Var var = null;

                if(data.length == 1){
                    var = VariableManagerImpl.instance().getGlobal(data[0]);
                } else if(data.length == 2){
                    var = VariableManagerImpl.instance().getPersonal(data[0], data[1]);
                }

                return var != null ? var.value() : (arr.length == 3 ? arr[2] : "");
            }

            return null;
        }

    }

    public static class VarPlayerHook implements PlaceholderHook {


        @Override
        public String replace(String placeholder, Player player) {
            if(player == null) return null;

            String[] arr = placeholder.split(":");

            if(arr.length > 1){
                Var var = VariableManagerImpl.instance().getPersonal(player.getName(), arr[1]);
                return var != null ? var.value() : (arr.length == 3 ? arr[2] : "");
            }

            return null;
        }

    }

    public static class VarTempHook implements PlaceholderHook {

        @Override
        public String replace(String placeholder, Player player) {
            String[] arr = placeholder.split(":");

            if(arr.length > 1) {
                return getVarTime(VariableManagerImpl.instance().getGlobal(arr[1]));
            }

            return null;
        }
    }

    public static class VarTempPlayerHook implements PlaceholderHook {

        @Override
        public String replace(String placeholder, Player player) {
            if(player == null) return null;

            String[] arr = placeholder.split(":");

            if(arr.length > 1){
                return getVarTime(VariableManagerImpl.instance().getPersonal(player.getName(), arr[1]));
            }

            return null;
        }
    }

    private static String getVarTime(Var var){
        return TimeUtil.getTimeString((var != null) ? var.expiry() - System.currentTimeMillis() : 0L);
    }

}
