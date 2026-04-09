package com.easyfarming.locations.special;

import com.easyfarming.EasyFarmingConfig;
import com.easyfarming.ItemRequirement;
import com.easyfarming.core.Location;
import com.easyfarming.core.Teleport;
import com.easyfarming.utils.Constants;
import net.runelite.api.coords.WorldPoint;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Location definition for Fossil Island (Bird Houses).
 * Updated with exact Region ID 14908 for the House on the Hill arrival point.
 */
public class FossilIslandLocationData {

    // Center point updated to your exact arrival coordinates (Plane 1 for the hill level)
    private static final WorldPoint FOSSIL_ISLAND_POINT = new WorldPoint(3763, 3870, 1);

    public static Location create(EasyFarmingConfig config, Supplier<List<ItemRequirement>> houseTeleportSupplier) {

        Location location = new Location(
                EasyFarmingConfig::enumOptionEnumFossilIslandTeleport,
                config,
                "Fossil Island",
                false // No Limpwurt patches here
        );

        // --- NEW: Added 4 Birdhouse tasks to keep the run active ---
        // This ensures the sidebar stays open when you arrive on the island.
        for (int i = 0; i < 4; i++) {
            location.addPatch(Constants.BIRDHOUSE_PATCH_IDS);
        }

        // Add the Digsite Pendant as the primary teleport option
        location.addTeleportOption(new Teleport(
                "Digsite_Pendant",
                Teleport.Category.ITEM,
                "Teleport to Fossil Island.",
                Constants.BASE_DIGSITE_PENDANT_ID, // Uses the ID from our Constants file
                "",
                0,
                0,
                14908, // The correct Region ID for the House on the Hill
                FOSSIL_ISLAND_POINT,
                Collections.singletonList(
                        new ItemRequirement(Constants.BASE_DIGSITE_PENDANT_ID, 1)
                )
        ));

        return location;
    }
}