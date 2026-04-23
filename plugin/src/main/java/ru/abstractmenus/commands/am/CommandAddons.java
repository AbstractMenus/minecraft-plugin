package ru.abstractmenus.commands.am;

import org.bukkit.command.CommandSender;
import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.addon.AddonManager;
import ru.abstractmenus.addon.AddonStatus;
import ru.abstractmenus.addon.LoadedAddon;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.commands.Command;

/** {@code /am addons [list|reload <name>|info <name>]} */
public class CommandAddons extends Command {

    public CommandAddons() {
        setUsage(
                Colors.of("&7/am addons list &e- list all AM-loaded addons"),
                Colors.of("&7/am addons reload <name> &e- reload a single addon"),
                Colors.of("&7/am addons info <name> &e- show addon metadata")
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
            case "list"   -> list(sender, am);
            case "reload" -> reload(sender, am, args);
            case "info"   -> info(sender, am, args);
            default       -> sender.sendMessage(getUsage());
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
            String color = switch (la.status()) {
                case ENABLED  -> "&a";
                case DISABLED -> "&7";
                case FAILED   -> "&c";
                case PENDING  -> "&e";
            };
            sender.sendMessage(Colors.of(color + "  " + la.conf().name()
                    + " &8v" + la.conf().version()
                    + " &7[" + la.status() + "]"));
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
        if (la.status() == AddonStatus.ENABLED) {
            sender.sendMessage(Colors.of("&aReloaded " + la.conf().name() + "."));
        } else {
            sender.sendMessage(Colors.of("&cReload failed: "
                    + (la.error() == null ? "unknown error" : la.error().getMessage())));
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
        var c = la.conf();
        sender.sendMessage(Colors.of("&e&l" + c.name() + " &7v" + c.version()));
        sender.sendMessage(Colors.of("&7  status: &f" + la.status()));
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
        if (la.status() == AddonStatus.FAILED && la.error() != null) {
            sender.sendMessage(Colors.of("&7  error: &c" + la.error().getMessage()));
        }
    }
}
