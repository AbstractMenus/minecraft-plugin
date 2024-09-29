package ru.abstractmenus.data.rules;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.util.RegionUtils;

import java.util.List;

public class RuleRegion implements Rule {

    private final List<String> regions;

    private RuleRegion(List<String> regions){
        this.regions = regions;
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        for (String reg : regions){
            String replaced = Handlers.getPlaceholderHandler().replace(player, reg);
            ProtectedRegion region = RegionUtils.getRegion(player.getWorld(), replaced);

            if(region != null && region.contains(
                    player.getLocation().getBlockX(),
                    player.getLocation().getBlockY(),
                    player.getLocation().getBlockZ()
            )){
                return true;
            }
        }

        return false;
    }

    public static class Serializer implements NodeSerializer<RuleRegion> {

        @Override
        public RuleRegion deserialize(Class<RuleRegion> type, ConfigNode node) throws NodeSerializeException {
            return new RuleRegion(node.getList(String.class));
        }

    }
}
