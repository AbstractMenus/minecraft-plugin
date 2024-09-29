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

            switch (placeholder) {
                default: return null;
                case "npc_id": return String.valueOf(npc.getId());
                case "npc_name": return npc.getName();
                case "npc_full_name": return npc.getFullName();
            }
        }
        return "";
    }
}
