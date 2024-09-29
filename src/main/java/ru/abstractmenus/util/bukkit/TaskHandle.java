package ru.abstractmenus.util.bukkit;

public interface TaskHandle {
    void cancel();

    boolean isCancelled();
}