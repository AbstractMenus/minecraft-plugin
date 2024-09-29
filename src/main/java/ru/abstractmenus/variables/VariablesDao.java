package ru.abstractmenus.variables;

import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.variables.Var;
import ru.abstractmenus.api.variables.VariableManager;
import ru.abstractmenus.util.FileUtils;
import ru.abstractmenus.util.bukkit.BukkitTasks;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Map;

public class VariablesDao {

    private final VariableManager varManager;
    private Connection connection;

    public VariablesDao(VariableManager varManager) {
        this.varManager = varManager;
    }

    public void create(String key, Var var) {
        executeUpdate("INSERT INTO variables (key, value, expiry) VALUES (?, ?, ?)", key, var.value(), var.expiry());
    }

    public void update(String key, Var var) {
        executeUpdate("UPDATE variables SET value=?, expiry=? WHERE key=?", var.value(), var.expiry(), key);
    }

    public void delete(String key) {
        executeUpdate("DELETE FROM variables WHERE key=?", key);
    }

    public void deleteExpired() {
        executeUpdate("DELETE FROM variables WHERE expiry > 0 AND expiry < ?", System.currentTimeMillis());
    }

    public void cacheAll(Map<String, Var> cache) {
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT * FROM variables");

            while (rs.next()) {
                String key = rs.getString(1);
                String value = rs.getString(2);
                long expiry = rs.getLong(3);
                String[] keyArr = key.split(":");

                if (keyArr.length < 2) continue;

                String name = keyArr[1];

                Var var = varManager.createBuilder()
                        .name(name)
                        .value(value)
                        .expiry(expiry)
                        .build();

                cache.put(key, var);
            }
        } catch (SQLException e) {
            Logger.severe("Cannot create statement:");
            e.printStackTrace();
        }
    }

    private void executeUpdate(String sql, Object... values) {
        BukkitTasks.runTaskAsync(() -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)){
                for (int i = 0; i < values.length; i++) {
                    statement.setObject(i+1, values[i]);
                }
                statement.executeUpdate();
            } catch (SQLException e) {
                Logger.severe("Cannot create prepared statement:");
                e.printStackTrace();
            }
        });
    }

    private void execute(String sql) {
        try (Statement statement = connection.createStatement()){
            statement.execute(sql);
        } catch (SQLException e) {
            Logger.severe("Cannot create statement:");
            e.printStackTrace();
        }
    }

    void init(Path folder) {
        Path path = Paths.get(folder.toString(), "variables.db");

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + path, "root", "");
            String sql = FileUtils.getResourceAsString("/variables.sql");
            execute(sql);
        } catch (SQLException e) {
            Logger.info("Unable to setup variables database");
            e.printStackTrace();
        }
    }

    void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Throwable ignore) {}
    }
}
