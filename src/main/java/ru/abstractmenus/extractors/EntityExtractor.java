package ru.abstractmenus.extractors;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.ValueExtractor;

public class EntityExtractor implements ValueExtractor {

    public static final EntityExtractor INSTANCE = new EntityExtractor();

    @Override
    public String extract(Object obj, String placeholder) {
        if (obj instanceof Entity) {
            Entity entity = (Entity) obj;

            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;

                switch (placeholder) {
                    case "entity_last_damage": return String.valueOf(living.getLastDamage());
                    case "entity_no_damage_ticks": return String.valueOf(living.getNoDamageTicks());
                    case "entity_killer": return living.getKiller() != null ? living.getKiller().getName() : null;
                    case "entity_eye_height": return String.valueOf(living.getEyeHeight());
                }
            }

            switch (placeholder) {
                case "entity_type": return entity.getType().toString();
                case "entity_id": return String.valueOf(entity.getEntityId());
                case "entity_uuid": return entity.getUniqueId().toString();
                case "entity_name": return entity.getName();
                case "entity_custom_name": return entity.getCustomName();
                case "entity_world": return entity.getWorld().getName();
                case "entity_loc_x": return String.valueOf(entity.getLocation().getX());
                case "entity_loc_y": return String.valueOf(entity.getLocation().getY());
                case "entity_loc_z": return String.valueOf(entity.getLocation().getZ());
                case "entity_pose": return entity.getPose().toString(); // 1.14+
                case "entity_facing": return entity.getFacing().toString();
                case "entity_ticks_lived": return String.valueOf(entity.getTicksLived());
            }
        }

        if (obj instanceof Player)
            return PlayerExtractor.INSTANCE.extract(obj, placeholder);

        return "";
    }
}
