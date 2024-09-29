package ru.abstractmenus.util.bukkit;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public final class Events {

    private static Plugin plugin;

    private Events(){}

    public static void setPlugin(Plugin p){
        plugin = p;
    }

    public static void register(Listener listener){
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    public static void unregister(Listener listener){
        HandlerList.unregisterAll(listener);
    }

    public static void unregisterAll(){
        HandlerList.unregisterAll(plugin);
    }
}
