package com.easyfarming.overlays.handlers;

import com.easyfarming.*;
import com.easyfarming.core.Teleport;
import com.easyfarming.overlays.highlighting.*;
import com.easyfarming.overlays.utils.ColorProvider;
import com.easyfarming.overlays.utils.PatchStateChecker;
import com.easyfarming.utils.Constants;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;

import javax.inject.Inject;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class FarmingStepHandler {
    private final Client client;
    private final EasyFarmingPlugin plugin;
    private final EasyFarmingConfig config;
    private final AreaCheck areaCheck;
    private final PatchHighlighter patchHighlighter;
    private final ItemHighlighter itemHighlighter;
    private final CompostHighlighter compostHighlighter;
    private final FarmerHighlighter farmerHighlighter;
    private final PatchStateChecker patchStateChecker;
    private final ColorProvider colorProvider;
    private final GameObjectHighlighter gameObjectHighlighter;
    private final EasyFarmingOverlay farmingHelperOverlay;

    public boolean herbPatchDone = false;
    public boolean flowerPatchDone = false;
    public boolean allotmentPatchDone = false;
    public boolean treePatchDone = false;
    public boolean fruitTreePatchDone = false;
    public boolean hopsPatchDone = false;
    public boolean birdHouseDone = false;

    private boolean herbPatchComposted = false;
    private boolean flowerPatchComposted = false;
    private boolean treePatchComposted = false;
    private boolean fruitTreePatchComposted = false;
    private boolean hopsPatchComposted = false;

    // Birdhouse Transition Trackers (Now tracking the "Built" state to bypass loading screens)
    private boolean vnBuilt = false;
    private boolean vsBuilt = false;
    private boolean mnBuilt = false;
    private boolean msBuilt = false;

    private final AllotmentPatchState allotmentPatchState = new AllotmentPatchState();

    @Inject
    public FarmingStepHandler(Client client, EasyFarmingPlugin plugin, EasyFarmingConfig config, AreaCheck areaCheck,
                              PatchHighlighter patchHighlighter, ItemHighlighter itemHighlighter,
                              CompostHighlighter compostHighlighter, FarmerHighlighter farmerHighlighter,
                              PatchStateChecker patchStateChecker, ColorProvider colorProvider,
                              EasyFarmingOverlay farmingHelperOverlay, GameObjectHighlighter gameObjectHighlighter) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.areaCheck = areaCheck;
        this.patchHighlighter = patchHighlighter;
        this.itemHighlighter = itemHighlighter;
        this.compostHighlighter = compostHighlighter;
        this.farmerHighlighter = farmerHighlighter;
        this.patchStateChecker = patchStateChecker;
        this.colorProvider = colorProvider;
        this.farmingHelperOverlay = farmingHelperOverlay;
        this.gameObjectHighlighter = gameObjectHighlighter;
    }

    public void herbSteps(Graphics2D graphics, Teleport teleport, String activeLocationName) {
        if (client.getLocalPlayer() == null) return;
        int currentRegionId = client.getLocalPlayer().getWorldLocation().getRegionID();
        String locationName = getLocationNameFromRegionId(currentRegionId);

        if (locationName == null || !locationName.equals(activeLocationName)) return;

        Color leftColor = colorProvider.getLeftClickColorWithAlpha();
        Color useItemColor = colorProvider.getHighlightUseItemWithAlpha();

        boolean isCivitas = (currentRegionId == 6192 || currentRegionId == 6459);
        int varbitId = isCivitas ? 4774 : getStandardHerbVarbit(currentRegionId);
        HerbPatchChecker.PlantState plantState = HerbPatchChecker.checkHerbPatch(client, varbitId);

        switch (plantState) {
            case HARVESTABLE: plugin.addTextToInfoBox("Harvest Herbs."); patchHighlighter.highlightHerbPatches(graphics, leftColor); break;
            case PLANT: plugin.addTextToInfoBox("Use Herb seed."); patchHighlighter.highlightHerbPatches(graphics, useItemColor); itemHighlighter.highlightHerbSeeds(graphics); break;
            case DEAD: plugin.addTextToInfoBox("Clear dead patch."); patchHighlighter.highlightHerbPatches(graphics, leftColor); break;
            case DISEASED: plugin.addTextToInfoBox("Use Plant cure."); patchHighlighter.highlightHerbPatches(graphics, leftColor); itemHighlighter.itemHighlight(graphics, ItemID.PLANT_CURE, useItemColor); break;
            case WEEDS:
                int val = client.getVarbitValue(varbitId);
                if (val == 3 || (isCivitas && val == 0)) { plugin.addTextToInfoBox("Use Herb seed."); patchHighlighter.highlightHerbPatches(graphics, useItemColor); itemHighlighter.highlightHerbSeeds(graphics); }
                else { plugin.addTextToInfoBox("Rake patch."); patchHighlighter.highlightHerbPatches(graphics, leftColor); }
                break;
            case GROWING:
                if (herbPatchComposted || patchStateChecker.patchIsCompostedForHerbPatch()) { herbPatchComposted = true; herbPatchDone = true; return; }
                plugin.addTextToInfoBox("Use Compost.");
                compostHighlighter.highlightCompost(graphics, true, false, false, 1);
                break;
        }
        applyPatchDirectionHintArrow(getHerbPatchWorldPoint(locationName));
    }

    public void flowerSteps(Graphics2D graphics, boolean farmLimps, String activeLocationName) {
        if (client.getLocalPlayer() == null) return;
        int currentRegionId = client.getLocalPlayer().getWorldLocation().getRegionID();
        String locationName = getLocationNameFromRegionId(currentRegionId);

        if (locationName == null || !locationName.equals(activeLocationName)) return;

        boolean isKastori = (locationName.equals("Kastori"));
        boolean isCivitas = (locationName.equals("Civitas illa Fortis"));

        if (farmLimps || isKastori || isCivitas) {
            Color leftColor = colorProvider.getLeftClickColorWithAlpha();
            int varbitId = (isKastori || isCivitas) ? 4773 : Constants.VARBIT_FLOWER_PATCH_STANDARD;
            FlowerPatchChecker.PlantState plantState = FlowerPatchChecker.checkFlowerPatch(client, varbitId);

            int patchObjectId = isKastori ? 56959 : (isCivitas ? 50660 : 0);

            switch (plantState) {
                case HARVESTABLE: plugin.addTextToInfoBox("Harvest Limpwurt."); patchHighlighter.highlightSpecificFlowerPatch(graphics, patchObjectId, leftColor); break;
                case WEEDS:
                    int val = client.getVarbitValue(varbitId);
                    if (val == 0 || val == 3) { plugin.addTextToInfoBox("Use Limpwurt seed."); itemHighlighter.highlightFlowerSeeds(graphics); }
                    else { plugin.addTextToInfoBox("Rake patch."); patchHighlighter.highlightSpecificFlowerPatch(graphics, patchObjectId, leftColor); }
                    break;
                case PLANT: plugin.addTextToInfoBox("Use Limpwurt seed."); itemHighlighter.highlightFlowerSeeds(graphics); break;
                case GROWING:
                    if (flowerPatchComposted || patchStateChecker.patchIsCompostedForFlowerPatch()) { flowerPatchComposted = true; flowerPatchDone = true; return; }
                    plugin.addTextToInfoBox("Use Compost.");
                    compostHighlighter.highlightCompost(graphics, false, false, false, 2);
                    break;
            }
            applyPatchDirectionHintArrow(getHerbPatchWorldPoint(locationName));
        } else { flowerPatchDone = true; }
    }

    public void allotmentSteps(Graphics2D graphics, Teleport teleport, String activeLocationName) {
        if (client.getLocalPlayer() == null) return;
        int currentRegionId = client.getLocalPlayer().getWorldLocation().getRegionID();
        String locationName = getLocationNameFromRegionId(currentRegionId);

        if (locationName == null || !locationName.equals(activeLocationName)) return;

        if (allotmentPatchState.getCurrentIndex() == 0) {
            allotmentCommonLogic(graphics, 0, locationName.equals("Civitas illa Fortis") ? 4771 : Constants.VARBIT_ALLOTMENT_PATCH_NORTH_A2, locationName);
            if (allotmentPatchState.isPatchCompleted(0)) allotmentPatchState.moveToNextPatch();
        } else {
            allotmentCommonLogic(graphics, 1, locationName.equals("Civitas illa Fortis") ? 4772 : Constants.VARBIT_ALLOTMENT_PATCH_SOUTH_B2, locationName);
            if (allotmentPatchState.isPatchCompleted(1)) { allotmentPatchDone = true; allotmentPatchState.reset(); }
        }
    }

    private void allotmentCommonLogic(Graphics2D graphics, int index, int varbitId, String locationName) {
        AllotmentPatchChecker.PlantState state = AllotmentPatchChecker.checkAllotmentPatch(client, varbitId);
        switch (state) {
            case HARVESTABLE: plugin.addTextToInfoBox("Harvest Allotment."); break;
            case PLANT: plugin.addTextToInfoBox("Use Allotment seed."); itemHighlighter.highlightAllotmentSeeds(graphics); break;
            case WEEDS:
                int val = client.getVarbitValue(varbitId);
                if (val == 0 || val == 3) { plugin.addTextToInfoBox("Use Allotment seed."); itemHighlighter.highlightAllotmentSeeds(graphics); }
                else { plugin.addTextToInfoBox("Rake Allotment."); }
                break;
            case GROWING:
                if (patchStateChecker.patchIsCompostedForAllotmentPatch()) { allotmentPatchState.markComposted(index); return; }
                plugin.addTextToInfoBox("Use Compost."); break;
        }
    }

    public void treeSteps(Graphics2D graphics, Teleport teleport, String activeLocationName) {
        if (client.getLocalPlayer() == null) return;
        int currentRegionId = client.getLocalPlayer().getWorldLocation().getRegionID();
        String locationName = getTreeLocationNameFromRegionId(currentRegionId);

        if (locationName == null || !locationName.equals(activeLocationName)) return;

        Color leftColor = colorProvider.getLeftClickColorWithAlpha();
        int varbitId = 4771;
        if (locationName.equals("Auburnvale")) varbitId = 4771;
        else if (currentRegionId == 4922) varbitId = 7905;

        TreePatchChecker.PlantState state = TreePatchChecker.checkTreePatch(client, varbitId);

        switch (state) {
            case HEALTHY: plugin.addTextToInfoBox("Check tree health."); patchHighlighter.highlightTreePatches(graphics, leftColor); break;
            case DEAD: plugin.addTextToInfoBox("Clear dead patch."); patchHighlighter.highlightTreePatches(graphics, leftColor); break;
            case DISEASED: plugin.addTextToInfoBox("Prune / Cure tree."); patchHighlighter.highlightTreePatches(graphics, leftColor); break;
            case REMOVE: plugin.addTextToInfoBox("Pay farmer to clear patch."); farmerHighlighter.highlightTreeFarmers(graphics); break;
            case WEEDS:
                int val = client.getVarbitValue(varbitId);
                if (val == 0 || val == 3) { plugin.addTextToInfoBox("Use Sapling."); itemHighlighter.highlightTreeSapling(graphics); }
                else { plugin.addTextToInfoBox("Rake patch."); patchHighlighter.highlightTreePatches(graphics, leftColor); }
                break;
            case PLANT: plugin.addTextToInfoBox("Use Sapling."); itemHighlighter.highlightTreeSapling(graphics); break;
            case GROWING:
                if (treePatchComposted || patchStateChecker.patchIsCompostedForTreePatch()) { treePatchComposted = true; treePatchDone = true; return; }
                plugin.addTextToInfoBox("Use Compost.");
                compostHighlighter.highlightCompost(graphics, false, true, false, 1);
                break;
        }
        applyPatchDirectionHintArrow(getTreePatchWorldPoint(locationName));
    }

    public void fruitTreeSteps(Graphics2D graphics, Teleport teleport, String activeLocationName) {
        if (client.getLocalPlayer() == null) return;
        int currentRegionId = client.getLocalPlayer().getWorldLocation().getRegionID();
        String locationName = getFruitTreeLocationNameFromRegionId(currentRegionId);

        if (locationName == null || !locationName.equals(activeLocationName)) return;

        Color leftColor = colorProvider.getLeftClickColorWithAlpha();

        int varbitId = 4771;
        if (locationName.equals("Kastori")) varbitId = 4772;
        else if (currentRegionId == 4922) varbitId = 7909;
        else if (locationName.equals("Gnome Stronghold")) varbitId = 4772;

        FruitTreePatchChecker.PlantState state = FruitTreePatchChecker.checkFruitTreePatch(client, varbitId);

        switch (state) {
            case HEALTHY: plugin.addTextToInfoBox("Check Fruit tree health."); patchHighlighter.highlightFruitTreePatches(graphics, leftColor); break;
            case DEAD: plugin.addTextToInfoBox("Clear dead patch."); patchHighlighter.highlightFruitTreePatches(graphics, leftColor); break;
            case DISEASED: plugin.addTextToInfoBox("Prune / Cure tree."); patchHighlighter.highlightFruitTreePatches(graphics, leftColor); break;
            case REMOVE: plugin.addTextToInfoBox("Pay farmer to clear patch."); farmerHighlighter.highlightFruitTreeFarmers(graphics); break;
            default:
            case WEEDS:
                int val = client.getVarbitValue(varbitId);
                if (val == 0 || val == 3) { plugin.addTextToInfoBox("Use Sapling."); itemHighlighter.highlightFruitTreeSapling(graphics); }
                else if (val > 3 && val < 50) { plugin.addTextToInfoBox("Harvest Fruit / Clear patch."); patchHighlighter.highlightFruitTreePatches(graphics, leftColor); }
                else { plugin.addTextToInfoBox("Rake patch."); patchHighlighter.highlightFruitTreePatches(graphics, leftColor); }
                break;
            case PLANT: plugin.addTextToInfoBox("Use Sapling."); itemHighlighter.highlightFruitTreeSapling(graphics); break;
            case GROWING:
                if (fruitTreePatchComposted || patchStateChecker.patchIsCompostedForFruitTreePatch()) { fruitTreePatchComposted = true; fruitTreePatchDone = true; return; }
                plugin.addTextToInfoBox("Use Compost.");
                compostHighlighter.highlightCompost(graphics, false, false, true, 1);
                break;
        }
        applyPatchDirectionHintArrow(getFruitTreePatchWorldPoint(locationName));
    }

    public void hopsSteps(Graphics2D graphics, Teleport teleport, String activeLocationName) {
        if (client.getLocalPlayer() == null) return;
        int currentRegionId = client.getLocalPlayer().getWorldLocation().getRegionID();
        String locationName = getHopsLocationNameFromRegionId(currentRegionId);

        if (locationName == null || !locationName.equals(activeLocationName)) return;

        int varbitId = 4771;
        HopsPatchChecker.PlantState state = HopsPatchChecker.checkHopsPatch(client, varbitId);

        switch (state) {
            case HARVESTABLE: plugin.addTextToInfoBox("Harvest Hops."); break;
            case PLANT: plugin.addTextToInfoBox("Use Hops seed."); itemHighlighter.highlightHopsSeeds(graphics); break;
            case GROWING: if (hopsPatchComposted || patchStateChecker.patchIsCompostedForHopsPatch()) { hopsPatchComposted = true; hopsPatchDone = true; return; } plugin.addTextToInfoBox("Use Compost."); break;
        }
        applyPatchDirectionHintArrow(getHopsPatchWorldPoint(locationName));
    }

    public void birdHouseSteps(Graphics2D graphics) {
        if (client.getLocalPlayer() == null) return;

        int regionId = client.getLocalPlayer().getWorldLocation().getRegionID();
        if (regionId != 14908 && regionId != 14906 && regionId != 14652 && regionId != 14651 && regionId != 14907) return;

        Color leftColor = colorProvider.getLeftClickColorWithAlpha();

        int vnState = client.getVarpValue(1628);
        int vsState = client.getVarpValue(1629);
        int mnState = client.getVarpValue(1626);
        int msState = client.getVarpValue(1627);

        // TRANSITION TRACKER: If it is built but unseeded (!= 0 and % 3 != 0), mark flag as true
        if (vnState != 0 && vnState % 3 != 0) vnBuilt = true;
        if (vsState != 0 && vsState % 3 != 0) vsBuilt = true;
        if (mnState != 0 && mnState % 3 != 0) mnBuilt = true;
        if (msState != 0 && msState % 3 != 0) msBuilt = true;

        // DONE CHECK: Must have been built first, AND is now back to a multiple of 3 (Seeded)
        boolean vnDone = vnBuilt && (vnState != 0 && vnState % 3 == 0);
        boolean vsDone = vsBuilt && (vsState != 0 && vsState % 3 == 0);
        boolean mnDone = mnBuilt && (mnState != 0 && mnState % 3 == 0);
        boolean msDone = msBuilt && (msState != 0 && msState % 3 == 0);

        boolean valleyDone = vnDone && vsDone;

        if (!valleyDone) {
            if (regionId == 14908 || regionId == 14907 || regionId == 14652 || regionId == 14651) {
                plugin.addTextToInfoBox("Go to Verdant Valley.");
            } else {
                plugin.addTextToInfoBox("Complete Verdant Valley Birdhouses.");
            }
        } else if (!mnDone) {
            if (regionId != 14652 && regionId != 14651) {
                plugin.addTextToInfoBox("Go to Mushroom Meadow.");
            } else {
                plugin.addTextToInfoBox("Complete North Meadow Birdhouse.");
            }
        } else if (!msDone) {
            plugin.addTextToInfoBox("Run south to final Birdhouse.");
        } else {
            birdHouseDone = true;
            plugin.clearLastMessage();
            return;
        }

        patchHighlighter.highlightBirdhousePatches(graphics, leftColor, vnDone, vsDone, mnDone, msDone);
    }

    private int getStandardHerbVarbit(int id) {
        if (id == 4922) return Constants.VARBIT_HERB_PATCH_FARMING_GUILD;
        if (id == 15148) return Constants.VARBIT_HERB_PATCH_HARMONY;
        if (id == 11321 || id == 11325) return Constants.VARBIT_HERB_PATCH_TROLL_WEISS;
        return Constants.VARBIT_HERB_PATCH_STANDARD;
    }

    private void applyPatchDirectionHintArrow(WorldPoint wp) { if (wp != null && client.getLocalPlayer() != null && !areaCheck.isPlayerWithinArea(wp, 15)) client.setHintArrow(wp); }
    public void clearHintArrow() { client.clearHintArrow(); }

    public void resetCompostStates() {
        herbPatchDone = flowerPatchDone = allotmentPatchDone = treePatchDone = fruitTreePatchDone = hopsPatchDone = birdHouseDone = false;
        herbPatchComposted = flowerPatchComposted = treePatchComposted = fruitTreePatchComposted = hopsPatchComposted = false;

        // Reset the Birdhouse Transition Trackers for the next run
        vnBuilt = false;
        vsBuilt = false;
        mnBuilt = false;
        msBuilt = false;

        allotmentPatchState.reset();
    }

    private String getLocationNameFromRegionId(int id) {
        switch (id) {
            case 10290: case 10548: return "Ardougne";
            case 11062: case 11317: case 11061: case 11318: return "Catherby";
            case 12083: case 11828: return "Falador";
            case 4922: return "Farming Guild";
            case 6967: return "Kourend";
            case 14391: return "Morytania";
            case 5167: case 5423: return "Kastori";
            case 6192: case 6459: return "Civitas illa Fortis";
            default: return "Unknown";
        }
    }

    private String getFruitTreeLocationNameFromRegionId(int id) {
        if (id == 5167 || id == 5423) return "Kastori";
        if (id == 4922) return "Farming Guild";
        if (id == 11317 || id == 11062 || id == 11061 || id == 11318) return "Catherby";
        if (id == 11058) return "Brimhaven";
        if (id == 9265) return "Lletya";
        if (id == 9781) return "Gnome Stronghold";
        if (id == 9777 || id == 9776 || id == 10033) return "Tree Gnome Village";
        return "Unknown";
    }

    private String getHopsLocationNameFromRegionId(int id) {
        if (id == 5421 || id == 5165) return "Aldarin";
        if (id == 12851) return "Lumbridge";
        if (id == 10551) return "Seers Village";
        if (id == 10288) return "Yanille";
        if (id == 11060) return "Entrana";
        return "Unknown";
    }

    private String getTreeLocationNameFromRegionId(int id) {
        if (id == 5427 || id == 5684 || id == 5428 || id == 5683) return "Auburnvale";
        if (id == 11828 || id == 12083) return "Falador";
        if (id == 4922) return "Farming Guild";
        if (id == 12594 || id == 12850) return "Lumbridge";
        if (id == 12854 || id == 12853) return "Varrock";
        if (id == 11573 || id == 11829) return "Taverley";
        if (id == 9781) return "Gnome Stronghold";
        return "Unknown";
    }

    private WorldPoint getHerbPatchWorldPoint(String name) {
        if (name == null) return null;
        switch (name) {
            case "Ardougne": return new WorldPoint(2670, 3374, 0);
            case "Catherby": return new WorldPoint(2813, 3463, 0);
            case "Falador": return new WorldPoint(3058, 3307, 0);
            case "Civitas illa Fortis": return new WorldPoint(1586, 3099, 0);
            case "Kastori": return new WorldPoint(1352, 3023, 0);
            default: return null;
        }
    }

    private WorldPoint getTreePatchWorldPoint(String name) {
        if (name == null) return null;
        switch (name) {
            case "Auburnvale": return new WorldPoint(1366, 3320, 0);
            case "Falador": return new WorldPoint(3000, 3373, 0);
            default: return null;
        }
    }

    private WorldPoint getFruitTreePatchWorldPoint(String name) {
        if (name == null) return null;
        switch (name) {
            case "Kastori": return new WorldPoint(1349, 3057, 0);
            case "Brimhaven": return new WorldPoint(2764, 3212, 0);
            default: return null;
        }
    }

    private WorldPoint getHopsPatchWorldPoint(String name) {
        if (name == null) return null;
        switch (name) {
            case "Aldarin": return new WorldPoint(1365, 2939, 0);
            default: return null;
        }
    }

    private static class AllotmentPatchState {
        private int currentIndex = 0;
        private final boolean[] completed = new boolean[2];
        public void reset() { currentIndex = 0; Arrays.fill(completed, false); }
        public int getCurrentIndex() { return currentIndex; }
        public boolean isPatchCompleted(int i) { return completed[i]; }
        public void markComposted(int i) { completed[i] = true; }
        public void moveToNextPatch() { currentIndex = 1; }
    }
}