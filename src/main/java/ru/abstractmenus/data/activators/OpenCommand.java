package ru.abstractmenus.data.activators;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.command.Command;
import ru.abstractmenus.command.CommandContext;
import ru.abstractmenus.command.CommandHandler;
import ru.abstractmenus.extractors.CommandExtractor;

import java.util.Arrays;

public class OpenCommand extends Activator {

    private final Command command;

    private OpenCommand(Command command) {
        this.command = command;
        command.setPlayerOnly(true);
        command.setHandler(new Handler(this));
        AbstractMenus.instance().getCommandManager().register(command);
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public ValueExtractor getValueExtractor() {
        return CommandExtractor.INSTANCE;
    }

    /*
    For overridden commands only
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onCommand(PlayerCommandPreprocessEvent event) {
        String[] arr = event.getMessage()
                .substring(1)
                .split(" ");

        if (arr.length > 0) {
            String label = arr[0];
            Command cmd = AbstractMenus.instance()
                    .getCommandManager()
                    .get(label);

            if (cmd == this.command && this.command.isOverride()) {
                String[] args = new String[0];

                if (arr.length > 1)
                    args = Arrays.copyOfRange(arr, 1, arr.length);

                AbstractMenus.instance().getCommandManager()
                        .process(event.getPlayer(), label, args);

                event.setCancelled(true);
            }
        }
    }

    private static class Handler implements CommandHandler {

        private final OpenCommand activator;

        public Handler(OpenCommand activator) {
            this.activator = activator;
        }

        @Override
        public void handle(CommandSender sender, CommandContext ctx) {
            if (sender instanceof Player) {
                activator.openMenu(ctx, (Player) sender);
            }
        }
    }

    public static class Serializer implements NodeSerializer<OpenCommand>{
        @Override
        public OpenCommand deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenCommand(node.getValue(Command.class));
        }
    }

}
