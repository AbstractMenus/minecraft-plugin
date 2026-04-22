package ru.abstractmenus.data.rules;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.datatype.TypeWorld;

public class RuleWorld implements Rule {

    private final TypeWorld world;

    private RuleWorld(TypeWorld world){
        this.world = world;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        return player.getWorld().equals(world.getWorld(player, menu));
    }

    public static class Serializer implements NodeSerializer<RuleWorld> {

        @Override
        public RuleWorld deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new RuleWorld(node.getValue(TypeWorld.class));
        }

    }
}
