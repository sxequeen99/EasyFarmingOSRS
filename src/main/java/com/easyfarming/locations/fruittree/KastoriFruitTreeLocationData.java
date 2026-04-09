package com.easyfarming.locations.fruittree;

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
 * Location definition for Kastori Fruit Tree patch.
 */
public class KastoriFruitTreeLocationData {

    // Coordinates you provided for the Fruit Tree
    private static final WorldPoint KASTORI_FRUIT_TREE_PATCH_POINT = new WorldPoint(1349, 3057, 0);

    /**
     * Creates Location for Kastori Fruit Tree patch.
     * @param config The EasyFarmingConfig instance
     * @param houseTeleportSupplier Supplier that provides house teleport item requirements
     * @return A Location instance for Kastori Fruit Tree patch
     */
    public static Location create(EasyFarmingConfig config, Supplier<List<ItemRequirement>> houseTeleportSupplier) {
        Location location = new Location(
                EasyFarmingConfig::enumFruitTreeKastoriTeleport,
                config,
                "Kastori",
                false // farmLimps
        );

        // Quetzal Whistle Teleport
        location.addTeleportOption(new Teleport(
                "Quetzal_whistle",
                Teleport.Category.ITEM,
                "Teleport to Kastori with Whistle.",
                ItemID.HG_QUETZALWHISTLE_BASIC,
                "",
                0,
                0,
                5167, // Your landing region
                KASTORI_FRUIT_TREE_PATCH_POINT,
                Arrays.asList(
                        new ItemRequirement(ItemID.HG_QUETZALWHISTLE_BASIC, 1),
                        new ItemRequirement(ItemID.COINS, 200) // For the farmer payment
                )
        ));

        return location;
    }
}