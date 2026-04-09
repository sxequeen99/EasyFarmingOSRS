package com.easyfarming.locations.tree;

import com.easyfarming.EasyFarmingConfig;
import com.easyfarming.ItemRequirement;
import com.easyfarming.core.Location;
import com.easyfarming.core.Teleport;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import com.easyfarming.EasyFarmingOverlay;
import java.util.Arrays;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class CivitasTreeLocationData {

    // TODO: Replace with the actual X, Y, Z coordinates later!
    private static final WorldPoint CIVITAS_TREE_PATCH_POINT = new WorldPoint(0, 0, 0);

    public static Location create(EasyFarmingConfig config, Supplier<List<ItemRequirement>> houseTeleportSupplier) {
        Location location = new Location(
                EasyFarmingConfig::enumOptionEnumCivitasTeleport, // Piggybacking on the existing setting
                config,
                "Civitas illa Fortis", // Merges perfectly into the UI
                false
        );

        location.addTeleportOption(new Teleport(
                "Quetzal_whistle", // Matches the dropdown option exactly
                Teleport.Category.ITEM,
                "Tree Route: Whistle to Colossal Wyrm Remains -> Walk to Oasis Hardwood.",
                ItemID.HG_QUETZALWHISTLE_BASIC,
                "",
                0,
                0,
                0, // TODO: Replace with the Region ID later!
                CIVITAS_TREE_PATCH_POINT,
                Arrays.asList(
                        new ItemRequirement(ItemID.HG_QUETZALWHISTLE_BASIC, 1),
                        // This links directly to the "Hardwood" category we built!
                        new ItemRequirement(EasyFarmingOverlay.BASE_HARDWOOD_SAPLING_ID, 1),
                        new ItemRequirement(ItemID.COINS, 200)
                )
        ));

        return location;
    }
}