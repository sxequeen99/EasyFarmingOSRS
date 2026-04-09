package com.easyfarming.locations;

import com.easyfarming.EasyFarmingConfig;
import com.easyfarming.ItemRequirement;
import com.easyfarming.core.Location;
import com.easyfarming.core.Teleport;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Location definition for Kastori Flower patch.
 */
public class KastoriLocationData {

    // The flower patch coordinates you provided
    private static final WorldPoint KASTORI_FLOWER_PATCH_POINT = new WorldPoint(1352, 3023, 0);

    /**
     * Gets the patch point for Kastori flower patch.
     * @return The WorldPoint for the Kastori flower patch
     */
    public static WorldPoint getPatchPoint() {
        return KASTORI_FLOWER_PATCH_POINT;
    }

    /**
     * Creates Location for Kastori.
     * @param config The EasyFarmingConfig instance
     * @param houseTeleportSupplier Supplier for house teleport requirements
     * @return A Location instance for Kastori
     */
    public static Location create(EasyFarmingConfig config, Supplier<List<ItemRequirement>> houseTeleportSupplier) {
        Location location = new Location(
                EasyFarmingConfig::enumOptionEnumKastoriTeleport,
                config,
                "Kastori",
                false // farmLimps (set to false as it's a flower patch)
        );

        // Quetzal Whistle
        location.addTeleportOption(new Teleport(
                "Quetzal_whistle",
                Teleport.Category.ITEM,
                "Teleport to Kastori with Whistle.",
                ItemID.HG_QUETZALWHISTLE_BASIC,
                "",
                0,
                0,
                5167, // Your landing region
                KASTORI_FLOWER_PATCH_POINT,
                Arrays.asList(
                        new ItemRequirement(ItemID.HG_QUETZALWHISTLE_BASIC, 1),
                        new ItemRequirement(ItemID.COINS, 200) // For whistle transport
                )
        ));

        return location;
    }
}