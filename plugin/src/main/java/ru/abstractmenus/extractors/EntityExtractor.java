package ru.abstractmenus.extractors;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.ValueExtractor;

public class EntityExtractor implements ValueExtractor {

    public static final EntityExtractor INSTANCE = new EntityExtractor();

    @Override
    public String extract(Object obj, String placeholder) {

        if (obj instanceof Entity entity) {
            if (entity instanceof LivingEntity livingEntity) {
                return switch (placeholder) {
                    case "entity_last_damage" -> String.valueOf(livingEntity.getLastDamage());
                    case "entity_no_damage_ticks" -> String.valueOf(livingEntity.getNoDamageTicks());
                    case "entity_killer" ->
                            livingEntity.getKiller() != null ? livingEntity.getKiller().getName() : null;
                    case "entity_eye_height" -> String.valueOf(livingEntity.getEyeHeight());
                    default -> "";
                };
            }

            return switch (placeholder) {
                case "entity_type" -> entity.getType().toString();
                case "entity_id" -> String.valueOf(entity.getEntityId());
                case "entity_uuid" -> entity.getUniqueId().toString();
                case "entity_name" -> entity.getName();
                case "entity_custom_name" -> entity.getCustomName();
                case "entity_world" -> entity.getWorld().getName();
                case "entity_loc_x" -> String.valueOf(entity.getLocation().getX());
                case "entity_loc_y" -> String.valueOf(entity.getLocation().getY());
                case "entity_loc_z" -> String.valueOf(entity.getLocation().getZ());
                case "entity_pose" -> entity.getPose().toString(); // 1.14+
                case "entity_facing" -> entity.getFacing().toString();
                case "entity_ticks_lived" -> String.valueOf(entity.getTicksLived());
                default -> "";
            };
        }
        if (obj instanceof Player)
            return PlayerExtractor.INSTANCE.extract(obj, placeholder);

        return "";
    }
}
