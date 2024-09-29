package ru.abstractmenus.services;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.MainConfig;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.Types;
import ru.abstractmenus.command.Command;
import ru.abstractmenus.data.actions.ActionInputChat;
import ru.abstractmenus.data.activators.OpenCommand;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.source.ConfigSources;
import ru.abstractmenus.menu.AbstractMenu;
import ru.abstractmenus.util.FileUtils;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.util.NMS;
import ru.abstractmenus.util.bukkit.BukkitTasks;
import ru.abstractmenus.util.bukkit.TaskHandle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MenuManager {

    private static MenuManager instance;

    private final Plugin plugin;
    private final Path menuFolder;

    private final Map<String, Menu> menus = new HashMap<>();
    private final Map<UUID, Menu> openedMenus = new ConcurrentHashMap<>();
    private final Map<UUID, ActionInputChat.InputAction> inputActions = new ConcurrentHashMap<>();

    private TaskHandle updateTask;
    private WatchService watcher;
    private Thread watcherThread;
    private long lastUpdated;

    public MenuManager(Plugin plugin, MainConfig conf) {
        instance = this;

        this.plugin = plugin;
        this.menuFolder = conf.getMenusFolder();
    }

    public static MenuManager instance() {
        return instance;
    }

    public Menu getOpenedMenu(Player player){
        return openedMenus.get(player.getUniqueId());
    }

    public Menu getMenu(String name){
        return menus.get(name.toLowerCase());
    }

    public void addMenu(String name, Menu menu){
        if(menu != null) {
            menus.put(name.toLowerCase(), menu);
        }
    }

    public void startUpdateTask() {
        if (updateTask == null) {
            updateTask = BukkitTasks.runTaskTimer(new UpdateTask(), 0, 1L);
        }
    }

    public void stopUpdateTask() {
        if(updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    public void openMenu(Player player, Menu menu) {
        openMenu(null, null, player, menu);
    }

    public void openMenu(Activator activator, Object ctx, Player player, Menu menu) {
        Menu cloned = menu.clone();

        if (cloned != null) {
            if (cloned instanceof AbstractMenu) {
                ((AbstractMenu) cloned).setActivatedBy(activator);
                ((AbstractMenu) cloned).setContext(ctx);
                ((AbstractMenu) cloned).setOpenListener(this::setOpenedMenu);
            }

            try {
                cloned.open(player);
            } catch (Throwable t) {
                closeMenu(player);
                throw t;
            }
        }
    }

    public void closeMenu(Player player) {
        closeMenu(player, true);
    }

    public void closeMenu(Player player, boolean closeInventory){
        Menu menu = removePlayerMenu(player);

        if(menu != null) {
            menu.close(player, closeInventory);
        }
    }

    public void refreshMenu(Player player){
        Menu opened = getOpenedMenu(player);

        if(opened != null) {
            opened.refresh(player);
        }
    }

    public void setOpenedMenu(Player player, Menu menu) {
        if (menu == null) {
            removePlayerMenu(player);
            return;
        }
        openedMenus.put(player.getUniqueId(), menu);
    }

    public Menu removePlayerMenu(Player player) {
        return openedMenus.remove(player.getUniqueId());
    }

    public ActionInputChat.InputAction getAndRemoveInputAction(Player player) {
        return inputActions.remove(player.getUniqueId());
    }

    public void saveInputAction(ActionInputChat.InputAction action) {
        inputActions.put(action.getPlayer().getUniqueId(), action);
    }

    public void loadMenus() throws Exception {
        unloadAll();
        loadExampleMenus();

        List<Path> files = getAllFiles(menuFolder);
        int menusCount = 0;

        for (Path file : files) {
            menusCount += loadFile(file);
        }

        try {
            Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
        } catch (Throwable ignore) {}

        Logger.info(String.format("Loaded %d menus", menusCount));
    }

    // TODO Rewrite this shit
    private int loadFile(Path file) {
        String filename = FileUtils.getBaseName(file.toFile().getName());
        ConfigNode conf;
        int count = 0;

        try {
            conf = ConfigurationLoader.builder()
                    .source(ConfigSources.path(file))
                    .serializers(Types.serializers())
                    .build()
                    .load();
        } catch (Throwable t) {
            Logger.severe("Cannot parse menu file '"+filename+"': " + t.getMessage());
            return 0;
        }

        ConfigNode menuList = conf.node("menus");

        if(!menuList.isNull()) {
            Map<String, ConfigNode> map = menuList.childrenMap();

            for (Map.Entry<String, ConfigNode> entry : map.entrySet()) {
                try {
                    Menu menu = entry.getValue().getValue(Menu.class);
                    addMenu(entry.getKey(), menu);
                    count++;
                } catch (NodeSerializeException ne) {
                    if (ne.getCauseNode() != null) {
                        int line = ne.getCauseNode().wrapped().origin().lineNumber();
                        Logger.severe(String.format("[Near line %d] Cannot load menu '%s' in file '%s': %s",
                                line, entry.getKey(), filename, ne.getMessage()));
                    }
                } catch (Throwable e) {
                    Logger.severe(String.format("Cannot load menu '%s' in file '%s': %s",
                            entry.getKey(), filename, e.getMessage()));
                    e.printStackTrace();
                }
            }
        } else {
            try {
                Menu menu = conf.getValue(Menu.class);
                addMenu(filename, menu);
                count++;
            } catch (NodeSerializeException ne) {
                if (ne.getCauseNode() != null) {
                    int line = ne.getCauseNode().wrapped().origin().lineNumber();
                    Logger.severe(String.format("[Near line %d] Cannot load menu '%s': %s",
                            line, filename, ne.getMessage()));
                }
            } catch (Throwable e) {
                Logger.severe("Cannot load menu '"+filename+"': " + e.getMessage());
                e.printStackTrace();
            }
        }

        return count;
    }

    public boolean serve() throws IOException {
        if (watcher != null) {
            watcher.close();

            try {
                watcherThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            watcher = null;
            watcherThread = null;
            return false;
        } else {
            watcher = FileSystems.getDefault().newWatchService();
            menuFolder.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            watcherThread = new Thread(() -> {
                while (true) {
                    WatchKey key;

                    try {
                        key = watcher.take();
                    } catch (Exception ex) {
                        return;
                    }

                    for (WatchEvent<?> event: key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();
                        Path file = menuFolder.resolve(filename);

                        if (Files.isRegularFile(file) && System.currentTimeMillis() > lastUpdated + 100) {
                            Logger.info("Detected changes in " + filename + ". Loading ...");
                            loadFile(file);
                            lastUpdated = System.currentTimeMillis();
                        }
                    }

                    if (!key.reset()) break;
                }
            });
            watcherThread.start();
            return true;
        }
    }

    public void unloadAll() {
        unregisterActivators();
        Bukkit.getOnlinePlayers().forEach(this::closeMenu);
        stopUpdateTask();
        menus.clear();
        openedMenus.clear();
        inputActions.clear();
    }

    private void unregisterActivators() {
        for (Menu menu : menus.values()) {
            List<Activator> activators = menu.getActivators();

            if(activators != null) {
                for (Activator activator : activators) {
                    if (activator instanceof OpenCommand) {
                        Command cmd = ((OpenCommand)activator).getCommand();

                        AbstractMenus.instance()
                                .getCommandManager()
                                .unregister(cmd);
                    }

                    HandlerList.unregisterAll(activator);
                }
            }
        }
    }

    private void loadExampleMenus() throws Exception {
        if(!Files.exists(menuFolder)){
            int version = NMS.getMinorVersion();

            Files.createDirectory(menuFolder);

            ConfigurationLoader.builder()
                    .source(ConfigSources.resource("/menu.conf", plugin)
                            .copyTo(menuFolder))
                    .serializers(Types.serializers())
                    .build()
                    .load();

            String animMenuPath;

            if (version >= 21) {
                animMenuPath = "/1_21/menu_anim.conf";
            } else if (version >= 13) {
                animMenuPath = "/1_13/menu_anim.conf";
            } else {
                animMenuPath = "/menu_anim.conf";
            }

            ConfigurationLoader.builder()
                    .source(ConfigSources.resource(animMenuPath, plugin)
                            .copyTo(menuFolder))
                    .serializers(Types.serializers())
                    .build()
                    .load();
        }
    }

    private List<Path> getAllFiles(Path folder) throws IOException {
        List<Path> files = new ArrayList<>();

        if (!Files.isDirectory(folder)) {
            files.add(folder);
            return files;
        }

        Files.list(folder).forEach((path) -> {
            try {
                if (Files.isDirectory(path)) {
                    files.addAll(getAllFiles(path));
                }
                if("conf".equalsIgnoreCase(FileUtils.getExtension(path.toFile().getName()))){
                    if(!checkInvisible(path)) {
                        files.add(path);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return files;
    }

    private boolean checkInvisible(Path file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))){
            String line = reader.readLine();
            return line != null && line.trim().equals("#invisible");
        } catch (IOException e) {
            return false;
        }
    }

    private class UpdateTask implements Runnable {

        @Override
        public void run() {
            for (Map.Entry<UUID, Menu> entry : openedMenus.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                entry.getValue().update(player);
            }
        }

    }
}