package ru.abstractmenus.data.actions;


import ru.abstractmenus.datatype.DataType;
import ru.abstractmenus.datatype.TypeEnum;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.datatype.*;

public class ActionGameModeSet implements Action {

    private final TypeEnum<GameMode> gameMode;

    private ActionGameModeSet(TypeEnum<GameMode> gameMode){
        this.gameMode = gameMode;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        GameMode mode = gameMode.getEnum(GameMode.class, player, menu);
        player.setGameMode(mode);
    }

    public static class Serializer implements NodeSerializer<ActionGameModeSet> {

        @Override
        public ActionGameModeSet deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            TypeEnum<GameMode> typeEnum;
            String rawEnum = node.getString("");

            if (DataType.hasPlaceholder(rawEnum)) {
                typeEnum = new TypeEnum<>(rawEnum);
            } else {
                typeEnum = new TypeEnum<>(GameMode.valueOf(rawEnum.toUpperCase()));
            }

            return new ActionGameModeSet(typeEnum);
        }

    }
}
