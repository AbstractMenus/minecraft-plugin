package ru.abstractmenus.commands.am;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.addon.AddonManager;
import ru.abstractmenus.addon.AddonStatus;
import ru.abstractmenus.addon.LoadedAddon;
import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.commands.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/** {@code /am addons [list|reload <name>|info <name>|load <name>|rescan]} */
public class CommandAddons extends Command {

    private static final List<String> SUBCOMMANDS =
            List.of("list", "reload", "info", "load", "rescan");

    /**
     * Strips legacy {@code &x}, section-sign {@code §x}, and hex
     * {@code <#RRGGBB>} formatting tokens from a string. Applied to every
     * addon-supplied value (names, versions, exception messages, etc.)
     * before it is rendered through {@link Colors#of} into operator chat.
     *
     * <p>Without this, a malicious addon could put {@code "&aOK, password=XYZ"}
     * in its addon.conf name or in an exception message and have it render
     * as a green "legitimate-looking" line in the operator's console-out
     * mirror. Threat model is operator-installed third-party addons - they
     * are semi-trusted but should not be able to social-engineer the
     * operator via formatted output.
     */
    private static final Pattern UNSAFE_FORMAT = Pattern.compile(
            "&[0-9a-fk-orA-FK-OR]|§[0-9a-fk-orA-FK-OR]|<#[0-9a-fA-F]{6}>");

    /**
     * Strips MiniMessage tags ({@code <red>}, {@code <click:run_command:...>},
     * {@code <hover:show_text:...>}, etc.). Applied alongside
     * {@link #UNSAFE_FORMAT} when MiniMessage rendering is enabled - without
     * it a malicious addon could put a {@code <click:run_command:/op X>}
     * tag in its name and turn an operator's {@code /am addons list} click
     * into privilege escalation. Conservatively matches anything shaped like
     * {@code <word ...>}; legit addon names don't need angle brackets.
     */
    private static final Pattern MM_TAG = Pattern.compile("<[/!?]?[a-zA-Z][^>]*>");

    private static String safe(String untrusted) {
        if (untrusted == null) return "";
        String stripped = UNSAFE_FORMAT.matcher(untrusted).replaceAll("");
        return MM_TAG.matcher(stripped).replaceAll("");
    }

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
            case "list" -> list(sender, am);
            case "reload" -> reload(sender, am, args);
            case "info" -> info(sender, am, args);
            case "load" -> load(sender, am, args);
            case "rescan" -> rescan(sender, am);
            default -> sender.sendMessage(getUsage());
        }
    }

    private void list(CommandSender sender, AddonManager am) {
        var path2 = am.loaded();
        Set<MenuExtension> path1 = pathOneExtensions(am);
        MenuExtension core = AbstractMenus.instance().getCore();

        int total = path2.size() + path1.size() + (core != null ? 1 : 0);
        if (total == 0) {
            sender.sendMessage(Colors.of("&7No addons."));
            return;
        }

        sender.sendMessage(Colors.of("&e&lAddons (" + total + "):"));

        // Path 2 — render unchanged.
        for (LoadedAddon la : path2) {
            String color = switch (la.getStatus()) {
                case ENABLED -> "&a";
                case DISABLED -> "&7";
                case FAILED -> "&c";
                case PENDING -> "&e";
            };
            sender.sendMessage(Colors.of(color + "  " + safe(la.getConf().name())
                    + " &8v" + safe(la.getConf().version())
                    + " &7[" + la.getStatus() + "]"));
        }

        // Path 1 — derived state from the JavaPlugin lifecycle when available.
        for (MenuExtension ext : path1) {
            boolean enabled = !(ext instanceof JavaPlugin jp) || jp.isEnabled();
            String color = enabled ? "&a" : "&c";
            String status = enabled ? "ENABLED" : "DISABLED";
            sender.sendMessage(Colors.of(color + "  " + safe(ext.name())
                    + " &8v" + safe(ext.version())
                    + " &7[" + status + "] &8[as-plugin]"));
        }

        // Built-in (CoreExtension) — last so the operator's eye lands on
        // operator-installed addons first.
        if (core != null) {
            sender.sendMessage(Colors.of("&a  " + safe(core.name())
                    + " &8v" + safe(core.version())
                    + " &7[ENABLED] &8[built-in]"));
        }
    }

    /**
     * Path 1 plugin-as-addons: every {@link MenuExtension} we see in the
     * registry footprint that is neither a Path 2 AM-loaded addon nor the
     * built-in {@code CoreExtension}.
     */
    private static Set<MenuExtension> pathOneExtensions(AddonManager am) {
        // Names of Path 2 addons - their MenuExtension instances must be
        // skipped from the "registry-footprint" set.
        Set<MenuExtension> path2Exts = new LinkedHashSet<>();
        for (LoadedAddon la : am.loaded()) {
            if (la.getExtension() != null) path2Exts.add(la.getExtension());
        }
        MenuExtension core = AbstractMenus.instance().getCore();

        Set<MenuExtension> path1 = new LinkedHashSet<>();
        for (MenuExtension ext : am.knownExtensions()) {
            if (ext == core) continue;
            if (path2Exts.contains(ext)) continue;
            path1.add(ext);
        }
        return path1;
    }

    private void reload(CommandSender sender, AddonManager am, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Colors.of("&cUsage: /am addons reload <name>"));
            return;
        }
        String name = args[1];
        var result = am.reload(name);
        if (result.isEmpty()) {
            sender.sendMessage(Colors.of("&cAddon '" + safe(name) + "' not found or no jar present."));
            return;
        }
        LoadedAddon la = result.get();
        if (la.getStatus() == AddonStatus.ENABLED) {
            sender.sendMessage(Colors.of("&aReloaded " + safe(la.getConf().name()) + "."));
        } else {
            sender.sendMessage(Colors.of("&cReload failed: "
                    + (la.getError() == null ? "unknown error" : safe(la.getError().getMessage()))));
        }
    }

    private void info(CommandSender sender, AddonManager am, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Colors.of("&cUsage: /am addons info <name>"));
            return;
        }
        String name = args[1];

        // Path 2 — full addon.conf metadata.
        var opt = am.get(name);
        if (opt.isPresent()) {
            renderPathTwoInfo(sender, opt.get());
            return;
        }

        // Built-in.
        MenuExtension core = AbstractMenus.instance().getCore();
        if (core != null && core.name().equalsIgnoreCase(name)) {
            sender.sendMessage(Colors.of("&e&l" + safe(core.name()) + " &7v" + safe(core.version())));
            sender.sendMessage(Colors.of("&7  status: &fENABLED &8[built-in]"));
            return;
        }

        // Path 1 — surface the JavaPlugin description when available.
        for (MenuExtension ext : pathOneExtensions(am)) {
            if (!ext.name().equalsIgnoreCase(name)) continue;
            renderPathOneInfo(sender, ext);
            return;
        }

        sender.sendMessage(Colors.of("&cAddon '" + safe(name) + "' not found."));
    }

    private static void renderPathTwoInfo(CommandSender sender, LoadedAddon la) {
        var c = la.getConf();
        sender.sendMessage(Colors.of("&e&l" + safe(c.name()) + " &7v" + safe(c.version())));
        sender.sendMessage(Colors.of("&7  status: &f" + la.getStatus()));
        if (!c.authors().isEmpty()) {
            sender.sendMessage(Colors.of("&7  authors: &f" + safe(String.join(", ", c.authors()))));
        }
        if (!c.description().isEmpty()) {
            sender.sendMessage(Colors.of("&7  description: &f" + safe(c.description())));
        }
        if (c.targetApiVersion() != null) {
            sender.sendMessage(Colors.of("&7  targetApiVersion: &f" + safe(c.targetApiVersion())));
        }
        if (!c.addonDependencies().isEmpty()) {
            sender.sendMessage(Colors.of("&7  addonDependencies: &f"
                    + safe(String.join(", ", c.addonDependencies()))));
        }
        if (!c.pluginDependencies().isEmpty()) {
            sender.sendMessage(Colors.of("&7  pluginDependencies: &f"
                    + safe(String.join(", ", c.pluginDependencies()))));
        }
        if (la.getStatus() == AddonStatus.FAILED && la.getError() != null) {
            sender.sendMessage(Colors.of("&7  error: &c" + safe(la.getError().getMessage())));
        }
    }

    private static void renderPathOneInfo(CommandSender sender, MenuExtension ext) {
        sender.sendMessage(Colors.of("&e&l" + safe(ext.name()) + " &7v" + safe(ext.version())
                + " &8[as-plugin]"));

        if (ext instanceof JavaPlugin jp) {
            PluginDescriptionFile desc = jp.getDescription();
            sender.sendMessage(Colors.of("&7  status: &f" + (jp.isEnabled() ? "ENABLED" : "DISABLED")));
            if (!desc.getAuthors().isEmpty()) {
                sender.sendMessage(Colors.of("&7  authors: &f"
                        + safe(String.join(", ", desc.getAuthors()))));
            }
            if (desc.getDescription() != null && !desc.getDescription().isEmpty()) {
                sender.sendMessage(Colors.of("&7  description: &f" + safe(desc.getDescription())));
            }
            if (!desc.getDepend().isEmpty()) {
                sender.sendMessage(Colors.of("&7  depend: &f"
                        + safe(String.join(", ", desc.getDepend()))));
            }
            if (!desc.getSoftDepend().isEmpty()) {
                sender.sendMessage(Colors.of("&7  softDepend: &f"
                        + safe(String.join(", ", desc.getSoftDepend()))));
            }
        } else {
            // Non-JavaPlugin Path 1 - rare but legal (an extension produced
            // by a plugin's onEnable that isn't the plugin instance itself).
            sender.sendMessage(Colors.of("&7  status: &fENABLED"));
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
            sender.sendMessage(Colors.of("&cNo unloaded addon named '" + safe(name) + "' found in addons/. "
                    + "Check the jar is in plugins/AbstractMenus/addons/ and addon.conf names it correctly."));
            return;
        }
        LoadedAddon la = result.get();
        if (la.getStatus() == AddonStatus.ENABLED) {
            sender.sendMessage(Colors.of("&aLoaded " + safe(la.getConf().name())
                    + " v" + safe(la.getConf().version()) + "."));
        } else {
            sender.sendMessage(Colors.of("&cLoad failed: "
                    + (la.getError() == null ? "unknown error" : safe(la.getError().getMessage()))));
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
            sender.sendMessage(Colors.of(color + "  " + safe(la.getConf().name())
                    + " &8v" + safe(la.getConf().version())
                    + " &7[" + la.getStatus() + "]"));
        }
    }

    /**
     * Names valid for {@code /am addons info <name>} tab-complete: every
     * Path 2 loaded addon, every Path 1 plugin-as-addon, and the built-in
     * core extension.
     */
    private static List<String> allInfoNames(AddonManager am) {
        List<String> names = new ArrayList<>();
        for (LoadedAddon la : am.loaded()) names.add(la.getConf().name());
        for (MenuExtension ext : pathOneExtensions(am)) names.add(ext.name());
        MenuExtension core = AbstractMenus.instance().getCore();
        if (core != null) names.add(core.name());
        return names;
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
                // reload only works on Path 2 (the only ones with a jar in
                // addons/ to re-read).
                case "reload" -> filterByPrefix(
                        () -> am.loaded().stream().map(la -> la.getConf().name()).iterator(),
                        prefix);
                // info works on all three: Path 2, Path 1, built-in core.
                case "info" -> filterByPrefix(allInfoNames(am), prefix);
                case "load" -> filterByPrefix(am.availableNotLoaded(), prefix);
                default -> Collections.emptyList();
            };
        }

        return Collections.emptyList();
    }

}
