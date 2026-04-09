package com.easyfarming.locations.tree;

import com.easyfarming.EasyFarmingConfig;
import com.easyfarming.ItemRequirement;
import com.easyfarming.core.Location;
import com.easyfarming.core.Teleport;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class AuburnvaleTreeLocationData {
    // Exact coordinates from your screenshot (+1 North to hit the dirt)
    private static final WorldPoint AUBURNVALE_TREE_PATCH_POINT = new WorldPoint(1366, 3320, 0);

    public static Location create(EasyFarmingConfig config, Supplier<List<ItemRequirement>> houseTeleportSupplier) {
        Location location = new Location(
                EasyFarmingConfig::enumTreeAuburnvaleTeleport,
                config,
                "Auburnvale",
                false
        );

        location.addTeleportOption(new Teleport(
                "Quetzal_whistle",
                Teleport.Category.ITEM,
                "Teleport to Auburnvale with Whistle.",
                ItemID.HG_QUETZALWHISTLE_BASIC,
                "",
                0,
                0,
                5684, // Landing Region ID
                AUBURNVALE_TREE_PATCH_POINT,
                Arrays.asList(
                        new ItemRequirement(ItemID.HG_QUETZALWHISTLE_BASIC, 1),
                        new ItemRequirement(ItemID.COINS, 200)
                )
        ));

        return location;
    }
}