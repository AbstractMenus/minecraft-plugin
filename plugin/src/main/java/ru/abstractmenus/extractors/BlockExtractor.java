package ru.abstractmenus.extractors;

import org.bukkit.block.Block;
import ru.abstractmenus.api.ValueExtractor;

public class BlockExtractor implements ValueExtractor {

    public static final BlockExtractor INSTANCE = new BlockExtractor();

    @Override
    public String extract(Object obj, String placeholder) {
        if (obj instanceof Block) {
            Block block = (Block) obj;

            return switch (placeholder) {
                default -> null;
                case "block_type" -> block.getType().toString();
                case "block_data" -> String.valueOf(block.getData()); // 1.12-
                case "block_world" -> block.getWorld().getName();
                case "block_x" -> String.valueOf(block.getX());
                case "block_y" -> String.valueOf(block.getY());
                case "block_z" -> String.valueOf(block.getZ());
                case "block_power" -> String.valueOf(block.getBlockPower());
                case "block_temp" -> String.valueOf(block.getTemperature());
                case "block_biome" -> block.getBiome().name();
            };
        }

        return "";
    }

}
