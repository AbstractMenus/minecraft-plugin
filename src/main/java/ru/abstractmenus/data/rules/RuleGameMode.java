package ru.abstractmenus.data.rules;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.util.StringUtil;
import ru.abstractmenus.datatype.TypeEnum;

public class RuleGameMode implements Rule {

    private final TypeEnum<GameMode> gameMode;

    private RuleGameMode(TypeEnum<GameMode> gameMode){
        this.gameMode = gameMode;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        return player.getGameMode().equals(gameMode.getEnum(GameMode.class, player, menu));
    }

    public static class Serializer implements NodeSerializer<RuleGameMode> {
        @Override
        public RuleGameMode deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            String name = node.getString("");

            try{
                return new RuleGameMode(new TypeEnum<>(GameMode.valueOf(name.toUpperCase())));
            } catch (IllegalArgumentException e){
                if (!StringUtil.contains(name, '%'))
                    throw new NodeSerializeException(node, "Cannot parse Game Mode " + name);

                return new RuleGameMode(new TypeEnum<>(name));
            }
        }

    }
}
