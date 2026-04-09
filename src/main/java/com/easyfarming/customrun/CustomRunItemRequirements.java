package com.easyfarming.customrun;

import com.easyfarming.EasyFarmingConfig;
import com.easyfarming.core.Location;
import com.easyfarming.core.Teleport;
import com.easyfarming.utils.Constants;
import com.easyfarming.utils.TeleportItemRequirements;
import net.runelite.api.Client;
import net.runelite.api.gameval.ItemID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the item requirement map for a custom run.
 * Updated to ignore hidden side-panel settings and rely 100% on the Main Config.
 */
public final class CustomRunItemRequirements {

    private CustomRunItemRequirements() {}

    public static Map<Integer, Integer> buildRequirements(
            com.easyfarming.customrun.LocationCatalog catalog,
            EasyFarmingConfig config,
            Client client,
            List<RunLocation> runLocations,
            boolean includeSecateurs,
            boolean includeDibber,
            boolean includeRake) {
        Map<Integer, Integer> allRequirements = new HashMap<>();
        if (catalog == null || runLocations == null || runLocations.isEmpty()) {
            return allRequirements;
        }

        int herbPatchCount = 0;
        int flowerPatchCount = 0;
        int allotmentPatchCount = 0;
        int treePatchCount = 0;
        int hardwoodPatchCount = 0;
        int fruitTreePatchCount = 0;
        int hopsPatchCount = 0;
        int compostPatchesTotal = 0;
        int birdHouseCount = 0;

        for (RunLocation rl : runLocations) {
            String name = rl.getLocationName();
            List<String> patchTypes = rl.getPatchTypes();
            if (name == null || patchTypes == null || patchTypes.isEmpty()) continue;

            Set<Location> processedLocations = new HashSet<>();

            // Loop through patches and grab teleports directly from the Main Config
            for (String patchType : patchTypes) {
                Location loc = catalog.getLocationForPatch(name, patchType);
                if (loc == null) continue;

                if (!processedLocations.add(loc)) continue;

                // FIX: Ignore the 'rl.getTeleportOption()' (the hidden side-panel ghost)
                // and always pull from your Main Config panel.
                Teleport teleport = loc.getSelectedTeleport();

                if (teleport != null) {
                    Map<Integer, Integer> req = teleport.getItemRequirements();
                    mergeTeleportRequirements(allRequirements, req);
                }
            }

            for (String patchType : patchTypes) {
                if (PatchTypes.HERB.equals(patchType)) { herbPatchCount++; compostPatchesTotal++; }
                if (PatchTypes.FLOWER.equals(patchType)) { flowerPatchCount++; compostPatchesTotal++; }
                if (PatchTypes.ALLOTMENT.equals(patchType)) {
                    List<Integer> patchIds = Constants.ALLOTMENT_PATCH_IDS_BY_LOCATION.get(name);
                    int n = (patchIds != null && !patchIds.isEmpty()) ? patchIds.size() : 2;
                    allotmentPatchCount += n; compostPatchesTotal += n;
                }
                if (PatchTypes.TREE.equals(patchType)) {
                    if ("Civitas illa Fortis".equals(name)) hardwoodPatchCount++;
                    else treePatchCount++;
                    compostPatchesTotal++;
                }
                if (PatchTypes.FRUIT_TREE.equals(patchType)) { fruitTreePatchCount++; compostPatchesTotal++; }
                if (PatchTypes.HOPS.equals(patchType)) { hopsPatchCount++; compostPatchesTotal++; }
                if (PatchTypes.BIRD_HOUSE.equals((patchType))) birdHouseCount++;
            }
        }

        Integer selectedCompost = selectedCompostId(config);
        int compostId = selectedCompost != null ? selectedCompost : -1;
        if (compostId != -1 && compostId != 0) {
            if (compostId == ItemID.BOTTOMLESS_COMPOST_BUCKET) {
                allRequirements.merge(ItemID.BOTTOMLESS_COMPOST_BUCKET, 1, Integer::sum);
            } else {
                allRequirements.merge(compostId, compostPatchesTotal, Integer::sum);
            }
        }

        if (herbPatchCount > 0) allRequirements.merge(ItemID.GUAM_SEED, herbPatchCount, Integer::sum);
        if (flowerPatchCount > 0) allRequirements.merge(ItemID.LIMPWURT_SEED, flowerPatchCount, Integer::sum);
        if (allotmentPatchCount > 0) allRequirements.merge(Constants.BASE_ALLOTMENT_SEED_ID, allotmentPatchCount * 3, Integer::sum);
        if (treePatchCount > 0) allRequirements.merge(Constants.BASE_TREE_SAPLING_ID, treePatchCount, Integer::sum);
        if (hardwoodPatchCount > 0) allRequirements.merge(ItemID.PLANTPOT_TEAK_SAPLING, hardwoodPatchCount, Integer::sum);
        if (fruitTreePatchCount > 0) allRequirements.merge(Constants.BASE_FRUIT_TREE_SAPLING_ID, fruitTreePatchCount, Integer::sum);
        if (hopsPatchCount > 0) allRequirements.merge(ItemID.BARLEY_SEED, hopsPatchCount, Integer::sum);
        if (birdHouseCount > 0) {
            allRequirements.merge(ItemID.HAMMER, 1, Integer::sum);
            allRequirements.merge(ItemID.CHISEL, 1, Integer::sum);
            allRequirements.merge(ItemID.LOGS, 4, Integer::sum);
            allRequirements.merge(ItemID.HAMMERSTONE_HOP_SEED, 40, Integer::sum);
        }

        int allTreesCount = treePatchCount + fruitTreePatchCount + hardwoodPatchCount;
        if (allTreesCount > 0) allRequirements.merge(ItemID.COINS, 200 * allTreesCount, Integer::sum);

        allRequirements.merge(ItemID.SPADE, 1, Integer::sum);
        if (includeDibber) allRequirements.merge(ItemID.DIBBER, 1, Integer::sum);
        if (includeRake) allRequirements.merge(ItemID.RAKE, 1, Integer::sum);
        if (includeSecateurs) allRequirements.merge(ItemID.FAIRY_ENCHANTED_SECATEURS, 1, Integer::sum);

        if (!TeleportItemRequirements.needsDramenStaffForFairyRings(client)) {
            allRequirements.remove(ItemID.DRAMEN_STAFF);
        }

        return allRequirements;
    }

    private static Integer selectedCompostId(EasyFarmingConfig config) {
        if (config == null) return null;
        EasyFarmingConfig.OptionEnumCompost selectedCompost = config.enumConfigCompost();
        switch (selectedCompost) {
            case Compost: return ItemID.BUCKET_COMPOST;
            case Supercompost: return ItemID.BUCKET_SUPERCOMPOST;
            case Ultracompost: return ItemID.BUCKET_ULTRACOMPOST;
            case Bottomless: return ItemID.BOTTOMLESS_COMPOST_BUCKET;
            default: return 0;
        }
    }

    private static void mergeTeleportRequirements(Map<Integer, Integer> into, Map<Integer, Integer> from) {
        if (from == null) return;
        for (Map.Entry<Integer, Integer> entry : from.entrySet()) {
            int itemId = entry.getKey();
            int quantity = entry.getValue();
            // Capability check for capes and rings to ensure only 1 is ever packed
            if (itemId == ItemID.SKILLCAPE_CONSTRUCTION || itemId == ItemID.SKILLCAPE_CONSTRUCTION_TRIMMED || itemId == ItemID.SKILLCAPE_MAX ||
                    itemId == ItemID.HG_QUETZALWHISTLE_BASIC || itemId == ItemID.HG_QUETZALWHISTLE_ENHANCED || itemId == ItemID.HG_QUETZALWHISTLE_PERFECTED ||
                    itemId == ItemID.LUMBRIDGE_RING_MEDIUM || itemId == ItemID.LUMBRIDGE_RING_HARD || itemId == ItemID.LUMBRIDGE_RING_ELITE ||
                    itemId == ItemID.ARDY_CAPE_MEDIUM || itemId == ItemID.ARDY_CAPE_HARD || itemId == ItemID.ARDY_CAPE_ELITE ||
                    itemId == ItemID.DRAMEN_STAFF || itemId == ItemID.SKILLCAPE_FARMING || itemId == ItemID.SKILLCAPE_FARMING_TRIMMED) {
                into.merge(itemId, quantity, (a, b) -> Math.min(1, a + b));
            } else {
                into.merge(itemId, quantity, Integer::sum);
            }
        }
    }
}