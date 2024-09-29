package ru.abstractmenus.command.args;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerArgument extends Argument {

    public PlayerArgument(String key) {
        super(key, Colors.of("&cInvalid player name"));
    }

    @Override
    public List<String> suggest(CommandSender sender) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    @Override
    public Object parse(CommandSender sender, String value) {
        return Bukkit.getPlayerExact(value);
    }

    public static class Serializer implements NodeSerializer<PlayerArgument> {

        @Override
        public PlayerArgument deserialize(Class<PlayerArgument> type, ConfigNode node) throws NodeSerializeException {
            return new PlayerArgument(node.node("key").getString());
        }

    }

}
