package ru.abstractmenus.commands;

import lombok.Getter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Command implements CommandExecutor, TabCompleter {

    @Getter
    private String permission;
    @Getter
    private String[] usage;
    private final Map<String, Command> subCommands = new HashMap<>();

    public Command() {
    }

    public Command(String permission) {
        this.permission = permission;
    }

    public Command setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public Command addSub(String arg, Command command) {
        subCommands.put(arg, command);
        return this;
    }

    public Command getSub(String arg) {
        return subCommands.get(arg);
    }

    public Command setUsage(String... usage) {
        this.usage = usage;
        return this;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!checkPermission(sender, this)) return false;

        if (args.length > 0) {
            // ArrayDeque (not Stack — Stack extends Vector and synchronises
            // every op for no benefit on a single-threaded dispatch path).
            Deque<Command> stack = new ArrayDeque<>();
            Command sub;

            for (String arg : args) {
                if (stack.isEmpty()) {
                    sub = getSub(arg);
                    if (sub != null) {
                        stack.push(sub);
                        continue;
                    }
                    break;
                }

                sub = stack.peek().getSub(arg);
                if (sub != null) {
                    stack.push(sub);
                    continue;
                }
                break;
            }

            if (!stack.isEmpty()) {
                Command command = stack.pop();
                if (!checkPermission(sender, command)) return false;
                command.execute(sender, Arrays.copyOfRange(args, stack.size() + 1, args.length));
                return true;
            }

            if (usage != null) {
                sender.sendMessage(usage);
            }
            return false;
        }

        execute(sender, args);
        return true;
    }

    private boolean checkPermission(CommandSender sender, Command command) {
        if (command.getPermission() != null) {
            return sender.hasPermission(command.getPermission());
        }
        return true;
    }

    public abstract void execute(CommandSender sender, String[] args);

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (!checkPermission(sender, this)) return Collections.emptyList();
        return tabComplete(sender, args);
    }

    /**
     * Recursive tab-completion entry point. Default behaviour: if the
     * user is typing the first arg, return registered subcommand keys
     * matching the current prefix; if a subcommand has already been
     * named, drill into it and forward {@code args[1..]}. Subclasses
     * override this method to add custom completions for typed values
     * (e.g. addon names, menu names, online players).
     *
     * <p>Suggestions are only returned for the *last* arg in {@code args}
     * (the one Bukkit considers the "in-progress" token).
     */
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 0) return Collections.emptyList();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> result = new ArrayList<>();
            for (Map.Entry<String, Command> e : subCommands.entrySet()) {
                if (!checkPermission(sender, e.getValue())) continue;
                if (e.getKey().toLowerCase().startsWith(prefix)) {
                    result.add(e.getKey());
                }
            }
            Collections.sort(result);
            return result;
        }
        Command sub = getSub(args[0]);
        if (sub == null) return Collections.emptyList();
        if (!checkPermission(sender, sub)) return Collections.emptyList();
        return sub.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    /**
     * Helper for subclasses overriding {@link #tabComplete}: filter
     * {@code candidates} to entries whose lowercase form starts with
     * {@code prefix.toLowerCase()}, and return them sorted alphabetically.
     *
     * <p>Pulled up to the base because at least three subcommand
     * implementations (CommandAddons being the largest) used to ship a
     * private static copy of this same five-line filter.
     */
    protected static List<String> filterByPrefix(Iterable<String> candidates, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String s : candidates) {
            if (s.toLowerCase().startsWith(lower)) result.add(s);
        }
        Collections.sort(result);
        return result;
    }
}
