package ru.abstractmenus.util.bukkit;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Scheduling facade.
 *
 * <p><strong>Why entity-aware variants exist.</strong> On Folia,
 * {@link #runTask(Runnable)} / {@link #runTaskLater(Runnable, long)} /
 * {@link #runTaskTimer(Runnable, long, long)} route to the global region
 * scheduler. The global scheduler is forbidden from touching entity state
 * (player.isOnline, inventory.setItem, player.teleport, etc.) - those
 * calls require the entity's owning region thread or they throw a
 * threading violation at runtime.
 *
 * <p>For any task that interacts with a specific player or entity, use the
 * {@code runForEntity*} variants below. On Folia they dispatch to the
 * entity's region scheduler; on Spigot/Paper non-Folia they fall through
 * to the regular main-thread scheduler (where entity affinity isn't a
 * concern).
 */
public final class BukkitTasks {

    @Setter
    private static Plugin plugin;
    @Setter
    private static FoliaLib foliaLib;

    private BukkitTasks() {
    }

    /**
     * True if the plugin lifecycle has wired up scheduling. False in pure-unit
     * tests that exercise actions/rules without booting the plugin. When false,
     * every scheduling method falls through to running the {@link Runnable}
     * synchronously on the calling thread, which matches what tests assume.
     */
    private static boolean initialised() {
        return foliaLib != null && plugin != null;
    }

    public static void runTask(Runnable runnable) {
        if (!initialised()) {
            runnable.run();
            return;
        }
        if (foliaLib.isFolia()) {
            foliaLib.getScheduler().runNextTick(task -> runnable.run());
            return;
        }
        new BukkitRunnable() {
            public void run() {
                runnable.run();
            }
        }.runTask(plugin);
    }

    public static void runTaskAsync(Runnable runnable) {
        if (!initialised()) {
            runnable.run();
            return;
        }
        if (foliaLib.isFolia()) {
            foliaLib.getScheduler().runAsync(task -> runnable.run());
            return;
        }
        new BukkitRunnable() {
            public void run() {
                runnable.run();
            }
        }.runTaskAsynchronously(plugin);
    }

    public static void runTaskLater(Runnable runnable, long delay) {
        if (!initialised()) {
            runnable.run();
            return;
        }
        if (foliaLib.isFolia()) {
            foliaLib.getScheduler().runLater(runnable, delay);
            return;
        }
        new BukkitRunnable() {
            public void run() {
                runnable.run();
            }
        }.runTaskLater(plugin, delay);
    }

    public static void runTaskLaterAsync(Runnable runnable, long delay) {
        if (!initialised()) {
            runnable.run();
            return;
        }
        if (foliaLib.isFolia()) {
            foliaLib.getScheduler().runLaterAsync(runnable, delay);
            return;
        }
        new BukkitRunnable() {
            public void run() {
                runnable.run();
            }
        }.runTaskLaterAsynchronously(plugin, delay);
    }

    public static TaskHandle runTaskTimer(Runnable runnable, long delay, long period) {
        if (!initialised()) {
            runnable.run();
            return TaskHandle.NOOP;
        }
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

    // -----------------------------------------------------------------
    //  Entity-aware variants
    //
    //  Use these when the work touches a specific player/entity. On Folia
    //  they dispatch via the entity's region scheduler; on non-Folia they
    //  fall through to runTask*. If the entity has been removed by the
    //  time the task fires, FoliaLib silently drops it (no work runs).
    // -----------------------------------------------------------------

    /** Run {@code runnable} on {@code entity}'s region (Folia) or next tick (non-Folia). */
    public static void runForEntity(Entity entity, Runnable runnable) {
        if (!initialised()) {
            runnable.run();
            return;
        }
        if (foliaLib.isFolia()) {
            foliaLib.getScheduler().runAtEntity(entity, task -> runnable.run());
            return;
        }
        runTask(runnable);
    }

    /** Run {@code runnable} on {@code entity}'s region after {@code delay} ticks. */
    public static void runForEntityLater(Entity entity, Runnable runnable, long delay) {
        if (!initialised()) {
            runnable.run();
            return;
        }
        if (foliaLib.isFolia()) {
            foliaLib.getScheduler().runAtEntityLater(entity, runnable, delay);
            return;
        }
        runTaskLater(runnable, delay);
    }

    /**
     * Repeating task pinned to {@code entity}'s region (Folia) or main thread
     * (non-Folia). On Folia the timer auto-stops if the entity is removed.
     */
    public static TaskHandle runForEntityTimer(Entity entity, Runnable runnable, long delay, long period) {
        if (!initialised()) {
            runnable.run();
            return TaskHandle.NOOP;
        }
        if (foliaLib.isFolia()) {
            WrappedTask wrappedTask = foliaLib.getScheduler()
                    .runAtEntityTimer(entity, runnable, delay, period);
            return new TaskHandle() {
                @Override public void cancel()        { wrappedTask.cancel(); }
                @Override public boolean isCancelled() { return wrappedTask.isCancelled(); }
            };
        }
        return runTaskTimer(runnable, delay, period);
    }

    /** True iff running on Folia. Useful for callers that need to branch on platform directly. */
    public static boolean isFolia() {
        return initialised() && foliaLib.isFolia();
    }

    public static TaskHandle runTaskTimerAsync(Runnable runnable, long delay, long period) {
        if (!initialised()) {
            runnable.run();
            return TaskHandle.NOOP;
        }
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
