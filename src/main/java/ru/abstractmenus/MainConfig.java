package ru.abstractmenus;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import ru.abstractmenus.hocon.api.ConfigNode;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public final class MainConfig {

    private boolean useVariables;
    private boolean syncVariables;
    private boolean bungeeCord;
    private boolean bungeePing;
    private boolean useWorldGuard;
    private boolean useMiniMessage;

    private String timeDay;
    private String timeHour;
    private String timeMinute;
    private String timeSecond;

    private Path menusFolder;
    private Path dbFolder;

    public void load(Plugin plugin, ConfigNode node) {
        useVariables = node.node("variables").getBoolean(true);
        syncVariables = node.node("syncVariables").getBoolean(false);
        bungeeCord = node.node("bungeecord").getBoolean(false);
        bungeePing = node.node("bungeePing").getBoolean(false);
        useWorldGuard = node.node("useWorldGuard").getBoolean(false);
        useMiniMessage = node.node("useMiniMessage").getBoolean(false);

        timeDay = node.node("time", "day").getString("d");
        timeHour = node.node("time", "hour").getString("h");
        timeMinute = node.node("time", "minute").getString("min");
        timeSecond = node.node("time", "second").getString("sec");

        String folder = node.node("menusFolder").getString("default");

        if (folder.equalsIgnoreCase("default")) {
            menusFolder = Paths.get(plugin.getDataFolder().getAbsolutePath(), "menus");
        } else {
            menusFolder = Paths.get(folder);
        }

        String dbPath = node.node("dbFolder").getString("default");

        if (!dbPath.equalsIgnoreCase("default")) {
            dbFolder = Paths.get(dbPath);
        } else {
            dbFolder = plugin.getDataFolder().toPath();
        }
    }
}
