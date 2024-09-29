package ru.abstractmenus.variables;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.scheduler.BukkitRunnable;
import ru.abstractmenus.MainConfig;
import ru.abstractmenus.api.variables.Var;
import ru.abstractmenus.api.variables.VarBuilder;
import ru.abstractmenus.api.variables.VariableManager;
import ru.abstractmenus.services.BungeeManager;
import ru.abstractmenus.util.NumberUtil;
import ru.abstractmenus.util.bukkit.BukkitTasks;
import ru.abstractmenus.util.bukkit.TaskHandle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class VariableManagerImpl implements VariableManager {

    private static final String GLOBAL_KEY = "#global";
    private static VariableManagerImpl instance;

    private final MainConfig config;
    private final VariablesDao dao;
    private final Map<String, Var> cache = new ConcurrentHashMap<>();

    private TaskHandle timerTask;

    public VariableManagerImpl(MainConfig conf) {
        instance = this;

        this.config = conf;
        dao = new VariablesDao(this);

        if(conf.isUseVariables()) {
            dao.init(conf.getDbFolder());
            dao.cacheAll(cache);
            startTimer();
        }
    }

    public boolean isSyncVars() {
        return config.isUseVariables() && config.isSyncVariables();
    }

    @Override
    public Var getGlobal(String name) {
        return cache.get(key(name));
    }

    @Override
    public Var getPersonal(String username, String name) {
        return cache.get(key(username, name));
    }

    @Override
    public void saveGlobal(Var var, boolean replace) {
        save(key(var.name()), var, replace);
    }

    @Override
    public void savePersonal(String username, Var var, boolean replace) {
        save(key(username, var.name()), var, replace);
    }

    @Override
    public void deleteGlobal(String name) {
        delete(key(name));
    }

    @Override
    public void deletePersonal(String username, String name) {
        delete(key(username, name));
    }

    @Override
    public VarBuilder createBuilder() {
        return new VarBuilderImpl();
    }

    public void shutdown() {
        stopTimer();
        dao.close();
    }

    public void modifyNumericGlobal(String name, Function<Double, Double> func) {
        modifyNumeric(key(name), func);
    }

    public void modifyNumericPersonal(String username, String name, Function<Double, Double> func) {
        modifyNumeric(key(username, name), func);
    }

    private void modifyNumeric(String key, Function<Double, Double> func) {
        Var variable = cache.get(key);
        String name;
        double value;

        if (variable == null) {
            name = key.split(":")[1];
            value = func.apply(0.0);
        } else {
            name = variable.name();
            try {
                value = func.apply(variable.doubleValue());
            } catch (Throwable ignore) { return; }
        }

        variable = createBuilder()
                .name(name)
                .value(String.valueOf(NumberUtil.round(value, 100)))
                .build();

        save(key, variable, true);
    }

    public void cache(String key, Var var) {
        cache.put(key, var);
    }

    private void save(String key, Var var, boolean replace) {
        if (var.isExpired()) return;

        Var cached = cache.get(key);

        if (cached != null) {
            if (!replace) return;
            dao.update(key, var);
        } else {
            dao.create(key, var);
        }

        cache(key, var);
        sync("save", key, var);
    }

    public void delete(String key) {
        cache.remove(key);
        dao.delete(key);
        sync("delete", key, null);
    }

    private void sync(String action, String key, Var var) {
        if (config.isSyncVariables()) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();

            out.writeUTF(action);
            out.writeUTF(key);

            if (action.equals("save") && var != null) {
                out.writeUTF(var.value());
                out.writeLong(var.expiry());
            }

            BungeeManager.instance().forwardMessage("SyncVar", out.toByteArray());
        }
    }

    private void startTimer() {
        stopTimer();

        if (timerTask == null || timerTask.isCancelled()) {
            timerTask = BukkitTasks.runTaskTimerAsync(this::timerTask, 0L, 20L);
        }
    }

    private void stopTimer() {
        if (timerTask != null) {
            try {
                if (timerTask.isCancelled()) return;
            } catch (Throwable ignore) {}

            timerTask.cancel();
        }
    }

    private void timerTask() {
        boolean removed = cache.entrySet()
                .removeIf(entry -> entry.getValue().isExpired());

        if (removed) dao.deleteExpired();
    }

    private String key(String scope, String name) {
        return scope.toLowerCase() + ":" + name.toLowerCase();
    }

    private String key(String name) {
        return key(GLOBAL_KEY, name);
    }

    public static VariableManagerImpl instance() {
        return instance;
    }
}
