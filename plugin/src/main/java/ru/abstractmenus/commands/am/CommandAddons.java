package ru.abstractmenus.commands.am;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.addon.AddonManager;
import ru.abstractmenus.addon.AddonStatus;
import ru.abstractmenus.addon.LoadedAddon;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.commands.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** {@code /am addons [list|reload <name>|info <name>|load <name>|rescan]} */
public class CommandAddons extends Command {

    private static final List<String> SUBCOMMANDS =
            List.of("list", "reload", "info", "load", "rescan");

    public CommandAddons() {
        setUsage(
                Colors.of("&7/am addons list &e- list all AM-loaded addons"),
                Colors.of("&7/am addons reload <name> &e- reload a single loaded addon"),
                Colors.of("&7/am addons info <name> &e- show addon metadata"),
                Colors.of("&7/am addons load <name> &e- load a new addon dropped at runtime"),
                Colors.of("&7/am addons rescan &e- scan addons/ and load any new jars")
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        AddonManager am = AbstractMenus.instance().getAddonManager();
        if (am == null) {
            sender.sendMessage(Colors.of("&cAddonManager not yet initialised."));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(getUsage());
            return;
        }

        switch (args[0].toLowerCase()) {
            case "list"    -> list(sender, am);
            case "reload"  -> reload(sender, am, args);
            case "info"    -> info(sender, am, args);
            case "load"    -> load(sender, am, args);
            case "rescan"  -> rescan(sender, am);
            default        -> sender.sendMessage(getUsage());
        }
    }

    private void list(CommandSender sender, AddonManager am) {
        var addons = am.loaded();
        if (addons.isEmpty()) {
            sender.sendMessage(Colors.of("&7No AM-loaded addons."));
            return;
        }
        sender.sendMessage(Colors.of("&e&lAddons (" + addons.size() + "):"));
        for (LoadedAddon la : addons) {
            String color = switch (la.getStatus()) {
                case ENABLED  -> "&a";
                case DISABLED -> "&7";
                case FAILED   -> "&c";
                case PENDING  -> "&e";
            };
            sender.sendMessage(Colors.of(color + "  " + la.getConf().name()
                    + " &8v" + la.getConf().version()
                    + " &7[" + la.getStatus() + "]"));
        }
    }

    private void reload(CommandSender sender, AddonManager am, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Colors.of("&cUsage: /am addons reload <name>"));
            return;
        }
        String name = args[1];
        var result = am.reload(name);
        if (result.isEmpty()) {
            sender.sendMessage(Colors.of("&cAddon '" + name + "' not found or no jar present."));
            return;
        }
        LoadedAddon la = result.get();
        if (la.getStatus() == AddonStatus.ENABLED) {
            sender.sendMessage(Colors.of("&aReloaded " + la.getConf().name() + "."));
        } else {
            sender.sendMessage(Colors.of("&cReload failed: "
                    + (la.getError() == null ? "unknown error" : la.getError().getMessage())));
        }
    }

    private void info(CommandSender sender, AddonManager am, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Colors.of("&cUsage: /am addons info <name>"));
            return;
        }
        var opt = am.get(args[1]);
        if (opt.isEmpty()) {
            sender.sendMessage(Colors.of("&cAddon '" + args[1] + "' not found."));
            return;
        }
        LoadedAddon la = opt.get();
        var c = la.getConf();
        sender.sendMessage(Colors.of("&e&l" + c.name() + " &7v" + c.version()));
        sender.sendMessage(Colors.of("&7  status: &f" + la.getStatus()));
        if (!c.authors().isEmpty()) {
            sender.sendMessage(Colors.of("&7  authors: &f" + String.join(", ", c.authors())));
        }
        if (!c.description().isEmpty()) {
            sender.sendMessage(Colors.of("&7  description: &f" + c.description()));
        }
        if (c.targetApiVersion() != null) {
            sender.sendMessage(Colors.of("&7  targetApiVersion: &f" + c.targetApiVersion()));
        }
        if (!c.addonDependencies().isEmpty()) {
            sender.sendMessage(Colors.of("&7  addonDependencies: &f"
                    + String.join(", ", c.addonDependencies())));
        }
        if (!c.pluginDependencies().isEmpty()) {
            sender.sendMessage(Colors.of("&7  pluginDependencies: &f"
                    + String.join(", ", c.pluginDependencies())));
        }
        if (la.getStatus() == AddonStatus.FAILED && la.getError() != null) {
            sender.sendMessage(Colors.of("&7  error: &c" + la.getError().getMessage()));
        }
    }

    private void load(CommandSender sender, AddonManager am, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Colors.of("&cUsage: /am addons load <name>"));
            return;
        }
        String name = args[1];
        var result = am.loadOne(name);
        if (result.isEmpty()) {
            sender.sendMessage(Colors.of("&cNo unloaded addon named '" + name + "' found in addons/. "
                    + "Check the jar is in plugins/AbstractMenus/addons/ and addon.conf names it correctly."));
            return;
        }
        LoadedAddon la = result.get();
        if (la.getStatus() == AddonStatus.ENABLED) {
            sender.sendMessage(Colors.of("&aLoaded " + la.getConf().name()
                    + " v" + la.getConf().version() + "."));
        } else {
            sender.sendMessage(Colors.of("&cLoad failed: "
                    + (la.getError() == null ? "unknown error" : la.getError().getMessage())));
        }
    }

    private void rescan(CommandSender sender, AddonManager am) {
        var newlyLoaded = am.rescan();
        if (newlyLoaded.isEmpty()) {
            sender.sendMessage(Colors.of("&7No new addons found."));
            return;
        }
        long enabled = newlyLoaded.stream()
                .filter(la -> la.getStatus() == AddonStatus.ENABLED).count();
        long failed = newlyLoaded.size() - enabled;
        sender.sendMessage(Colors.of("&aRescan: " + enabled + " loaded"
                + (failed > 0 ? ", &c" + failed + " failed" : "") + "&a."));
        for (LoadedAddon la : newlyLoaded) {
            String color = la.getStatus() == AddonStatus.ENABLED ? "&a" : "&c";
            sender.sendMessage(Colors.of(color + "  " + la.getConf().name()
                    + " &8v" + la.getConf().version()
                    + " &7[" + la.getStatus() + "]"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 0) return Collections.emptyList();

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> result = new ArrayList<>();
            for (String s : SUBCOMMANDS) {
                if (s.startsWith(prefix)) result.add(s);
            }
            return result;
        }

        if (args.length == 2) {
            AddonManager am = AbstractMenus.instance().getAddonManager();
            if (am == null) return Collections.emptyList();
            String prefix = args[1].toLowerCase();
            return switch (args[0].toLowerCase()) {
                case "reload", "info" -> filterByPrefix(
                        am.loaded().stream().map(la -> la.getConf().name()).toList(),
                        prefix);
                case "load" -> filterByPrefix(am.availableNotLoaded(), prefix);
                default -> Collections.emptyList();
            };
        }

        return Collections.emptyList();
    }

    private static List<String> filterByPrefix(List<String> source, String prefix) {
        List<String> result = new ArrayList<>();
        for (String s : source) {
            if (s.toLowerCase().startsWith(prefix)) result.add(s);
        }
        Collections.sort(result);
        return result;
    }
}
