package ru.abstractmenus.extractors;

import org.bukkit.block.Block;
import ru.abstractmenus.api.ValueExtractor;

public class BlockExtractor implements ValueExtractor {

    public static final BlockExtractor INSTANCE = new BlockExtractor();

    @Override
    public String extract(Object obj, String placeholder) {
        if (obj instanceof Block) {
            Block block = (Block) obj;

            switch (placeholder) {
                default: return null;
                case "block_type": return block.getType().toString();
                case "block_data": return String.valueOf(block.getData()); // 1.12-
                case "block_world": return block.getWorld().getName();
                case "block_x": return String.valueOf(block.getX());
                case "block_y": return String.valueOf(block.getY());
                case "block_z": return String.valueOf(block.getZ());
                case "block_power": return String.valueOf(block.getBlockPower());
                case "block_temp": return String.valueOf(block.getTemperature());
                case "block_biome": return block.getBiome().name();
            }
        }

        return "";
    }

}
