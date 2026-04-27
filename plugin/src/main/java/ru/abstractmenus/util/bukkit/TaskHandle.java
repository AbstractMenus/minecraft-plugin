package ru.abstractmenus.util.bukkit;

public interface TaskHandle {
    void cancel();

    boolean isCancelled();

    /** Sentinel returned for tests / uninitialised contexts where no real timer was scheduled. */
    TaskHandle NOOP = new TaskHandle() {
        @Override public void cancel() {}
        @Override public boolean isCancelled() { return true; }
    };
}