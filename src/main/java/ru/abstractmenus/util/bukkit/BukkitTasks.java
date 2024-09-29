package ru.abstractmenus.util.bukkit;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.Setter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class BukkitTasks {

    @Setter
    private static Plugin plugin;
    @Setter
    private static FoliaLib foliaLib;

    private BukkitTasks(){}

    public static void runTask(Runnable runnable){
        if (foliaLib.isFolia()) {
            foliaLib.getScheduler().runNextTick(task -> runnable.run());
            return;
        }
        new BukkitRunnable(){
            public void run(){
                runnable.run();
            }
        }.runTask(plugin);
    }

    public static void runTaskAsync(Runnable runnable){
        if (foliaLib.isFolia()) {
            foliaLib.getScheduler().runAsync(task -> runnable.run());
            return;
        }
        new BukkitRunnable(){
            public void run(){
                runnable.run();
            }
        }.runTaskAsynchronously(plugin);
    }

    public static void runTaskLater(Runnable runnable, long delay){
        if (foliaLib.isFolia()) {
            foliaLib.getScheduler().runLater(runnable, delay);
            return;
        }
        new BukkitRunnable(){
            public void run(){
                runnable.run();
            }
        }.runTaskLater(plugin, delay);
    }

    public static void runTaskLaterAsync(Runnable runnable, long delay){
        if (foliaLib.isFolia()) {
            foliaLib.getScheduler().runLaterAsync(runnable, delay);
            return;
        }
        new BukkitRunnable(){
            public void run(){
                runnable.run();
            }
        }.runTaskLaterAsynchronously(plugin, delay);
    }

    public static TaskHandle runTaskTimer(Runnable runnable, long delay, long period) {
        if (foliaLib.isFolia()) {
            WrappedTask wrappedTask = foliaLib.getScheduler().runTimer(runnable, delay, period);
            return new TaskHandle() {
                @Override
                public void cancel() {
                    wrappedTask.cancel();
                }

                @Override
                public boolean isCancelled() {
                    return wrappedTask.isCancelled();
                }
            };
        } else {
            BukkitRunnable bukkitRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            };
            bukkitRunnable.runTaskTimer(plugin, delay, period);
            return new TaskHandle() {
                @Override
                public void cancel() {
                    bukkitRunnable.cancel();
                }

                @Override
                public boolean isCancelled() {
                    return bukkitRunnable.isCancelled();
                }
            };
        }
    }

    public static TaskHandle runTaskTimerAsync(Runnable runnable, long delay, long period) {
        if (foliaLib.isFolia()) {
            WrappedTask wrappedTask = foliaLib.getScheduler().runTimerAsync(runnable, delay, period);
            return new TaskHandle() {
                @Override
                public void cancel() {
                    wrappedTask.cancel();
                }

                @Override
                public boolean isCancelled() {
                    return wrappedTask.isCancelled();
                }
            };
        } else {
            BukkitRunnable bukkitRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            };
            bukkitRunnable.runTaskTimerAsynchronously(plugin, delay, period);
            return new TaskHandle() {
                @Override
                public void cancel() {
                    bukkitRunnable.cancel();
                }

                @Override
                public boolean isCancelled() {
                    return bukkitRunnable.isCancelled();
                }
            };
        }
    }

}
