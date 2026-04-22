package ru.abstractmenus.extractors;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import ru.abstractmenus.api.ValueExtractor;

public class RegionExtractor implements ValueExtractor {

    public static final RegionExtractor INSTANCE = new RegionExtractor();

    @Override
    public String extract(Object obj, String placeholder) {
        if (obj instanceof ProtectedRegion) {
            ProtectedRegion region = (ProtectedRegion) obj;

            return switch (placeholder) {
                default -> null;
                case "region_id" -> region.getId();
                case "region_priority" -> String.valueOf(region.getPriority());
                case "region_type" -> region.getType().getName();
                case "region_owners" -> region.getOwners().toPlayersString();
                case "region_members" -> region.getMembers().toPlayersString();
                case "region_owners_amount" -> String.valueOf(region.getOwners().size());
                case "region_members_amount" -> String.valueOf(region.getMembers().size());
            };
        }
        return "";
    }

}
