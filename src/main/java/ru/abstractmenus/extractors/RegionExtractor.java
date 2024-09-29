package ru.abstractmenus.extractors;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import ru.abstractmenus.api.ValueExtractor;

public class RegionExtractor implements ValueExtractor {

    public static final RegionExtractor INSTANCE = new RegionExtractor();

    @Override
    public String extract(Object obj, String placeholder) {
        if (obj instanceof ProtectedRegion) {
            ProtectedRegion region = (ProtectedRegion) obj;

            switch (placeholder) {
                default: return null;
                case "region_id": return region.getId();
                case "region_priority": return String.valueOf(region.getPriority());
                case "region_type": return region.getType().getName();
                case "region_owners": return region.getOwners().toPlayersString();
                case "region_members": return region.getMembers().toPlayersString();
                case "region_owners_amount": return String.valueOf(region.getOwners().size());
                case "region_members_amount": return String.valueOf(region.getMembers().size());
            }
        }
        return "";
    }

}
