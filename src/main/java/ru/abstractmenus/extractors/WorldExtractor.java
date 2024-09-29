package ru.abstractmenus.extractors;

import org.bukkit.World;
import ru.abstractmenus.api.ValueExtractor;

public class WorldExtractor implements ValueExtractor {

    public static final WorldExtractor INSTANCE = new WorldExtractor();

    @Override
    public String extract(Object obj, String placeholder) {
        if (obj instanceof World) {
            World world = (World) obj;

            switch (placeholder) {
                case "world_name": return world.getName();
                case "world_difficulty": return world.getDifficulty().name();
                case "world_max_height": return String.valueOf(world.getMaxHeight());
                case "world_pvp": return String.valueOf(world.getPVP());
                case "world_seed": return String.valueOf(world.getSeed());
                case "world_time": return String.valueOf(world.getTime());
                case "world_type": return world.getWorldType() != null ? world.getWorldType().getName() : "";
                case "world_entities": return String.valueOf(world.getEntities().size());
                case "world_players": return String.valueOf(world.getPlayers().size());
            }
        }

        return "";
    }

}
