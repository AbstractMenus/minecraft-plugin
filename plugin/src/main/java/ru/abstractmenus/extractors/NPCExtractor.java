package ru.abstractmenus.extractors;

import net.citizensnpcs.api.npc.NPC;
import ru.abstractmenus.api.ValueExtractor;

public class NPCExtractor implements ValueExtractor {

    public static final NPCExtractor INSTANCE = new NPCExtractor();

    @Override
    public String extract(Object obj, String placeholder) {
        if (obj instanceof NPC) {
            NPC npc = (NPC) obj;

            if (placeholder.startsWith("npc_entity_")) {
                return EntityExtractor.INSTANCE.extract(npc.getBukkitEntity(), placeholder.substring(4));
            }

            return switch (placeholder) {
                default -> null;
                case "npc_id" -> String.valueOf(npc.getId());
                case "npc_name" -> npc.getName();
                case "npc_full_name" -> npc.getFullName();
            };
        }
        return "";
    }
}
