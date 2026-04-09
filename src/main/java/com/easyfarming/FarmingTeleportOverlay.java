package com.easyfarming;

import java.awt.*;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import com.easyfarming.core.Teleport;
import com.easyfarming.core.Location;
import com.easyfarming.customrun.PatchTypes;
import com.easyfarming.customrun.CustomRun;
import com.easyfarming.customrun.RunLocation;
import com.easyfarming.overlays.handlers.NavigationHandler;
import com.easyfarming.overlays.handlers.FarmingStepHandler;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

public class FarmingTeleportOverlay extends Overlay {
    private final Client client;
    private final EasyFarmingPlugin plugin;

    @Inject
    private EasyFarmingOverlay farmingHelperOverlay;
    @Inject
    private EasyFarmingOverlayInfoBox farmingHelperOverlayInfoBox;
    @Inject
    private NavigationHandler navigationHandler;
    @Inject
    private FarmingStepHandler farmingStepHandler;

    private boolean customRunMode = false;
    private List<RunLocation> customRunLocations = new ArrayList<>();
    private String activeCustomRunName = null;

    private int currentPatchTypeIndex = 0;
    private int currentLocationIndex = 0;
    private boolean startSubCases = false;
    private boolean farmLimps = false;

    @Inject
    public FarmingTeleportOverlay(EasyFarmingPlugin plugin, Client client) {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.plugin = plugin;
        this.client = client;
    }

    public boolean isCustomRunMode() { return customRunMode; }
    public List<RunLocation> getCustomRunLocations() { return customRunLocations; }
    public String getActiveCustomRunName() { return activeCustomRunName; }

    private Location getCurrentLocationForCustomRun() {
        if (!customRunMode || customRunLocations.isEmpty() || currentLocationIndex >= customRunLocations.size()) return null;
        RunLocation rl = customRunLocations.get(currentLocationIndex);
        List<String> patchTypes = rl.getPatchTypes();
        if (patchTypes == null || patchTypes.isEmpty() || currentPatchTypeIndex >= patchTypes.size()) return null;

        String patchType = patchTypes.get(currentPatchTypeIndex);
        Location loc = plugin.getLocationCatalog().getLocationForPatch(rl.getLocationName(), patchType);
        if (loc != null && rl.getTeleportOption() != null) loc.setOverrideTeleportEnumOption(rl.getTeleportOption());
        return loc;
    }

    private String getCurrentPatchTypeForCustomRun() {
        if (!customRunMode || customRunLocations.isEmpty() || currentLocationIndex >= customRunLocations.size()) return null;
        RunLocation rl = customRunLocations.get(currentLocationIndex);
        List<String> patchTypes = rl.getPatchTypes();
        if (patchTypes == null || currentPatchTypeIndex >= patchTypes.size()) return null;
        return patchTypes.get(currentPatchTypeIndex);
    }

    private void navigateToCurrentLocation(Graphics2D graphics) {
        Location location = getCurrentLocationForCustomRun();
        if (location == null) { moveToNextLocation(); return; }
        navigationHandler.gettingToLocation(graphics, location, getCurrentPatchTypeForCustomRun());
        if (navigationHandler.isAtDestination) {
            startSubCases = true;
            if (location.getFarmLimps()) farmLimps = true;
            farmingStepHandler.clearHintArrow();
        }
    }

    private void handleCustomRunSteps(Graphics2D graphics) {
        Location location = getCurrentLocationForCustomRun();
        String patchType = getCurrentPatchTypeForCustomRun();
        if (location == null || patchType == null) { moveToNextLocation(); return; }
        Teleport teleport = location.getSelectedTeleport();

        switch (patchType) {
            case PatchTypes.HERB:
                farmingStepHandler.herbSteps(graphics, teleport, location.getName());
                if (farmingStepHandler.herbPatchDone) { farmingStepHandler.herbPatchDone = false; moveToNextPatchOrLocation(); }
                break;
            case PatchTypes.FLOWER:
                farmingStepHandler.flowerSteps(graphics, farmLimps, location.getName());
                if (farmingStepHandler.flowerPatchDone) { farmingStepHandler.flowerPatchDone = false; moveToNextPatchOrLocation(); }
                break;
            case PatchTypes.ALLOTMENT:
                farmingStepHandler.allotmentSteps(graphics, teleport, location.getName());
                if (farmingStepHandler.allotmentPatchDone) { farmingStepHandler.allotmentPatchDone = false; moveToNextPatchOrLocation(); }
                break;
            case PatchTypes.TREE:
                farmingStepHandler.treeSteps(graphics, teleport, location.getName());
                if (farmingStepHandler.treePatchDone) { farmingStepHandler.treePatchDone = false; moveToNextPatchOrLocation(); }
                break;
            case PatchTypes.FRUIT_TREE:
                farmingStepHandler.fruitTreeSteps(graphics, teleport, location.getName());
                if (farmingStepHandler.fruitTreePatchDone) { farmingStepHandler.fruitTreePatchDone = false; moveToNextPatchOrLocation(); }
                break;
            case PatchTypes.HOPS:
                farmingStepHandler.hopsSteps(graphics, teleport, location.getName());
                if (farmingStepHandler.hopsPatchDone) { farmingStepHandler.hopsPatchDone = false; moveToNextPatchOrLocation(); }
                break;
            case PatchTypes.BIRD_HOUSE:
                farmingStepHandler.birdHouseSteps(graphics);
                if (farmingStepHandler.birdHouseDone) { farmingStepHandler.birdHouseDone = false; moveToNextPatchOrLocation(); }
                break;
            default: moveToNextPatchOrLocation();
        }
    }

    private void moveToNextPatchOrLocation() {
        RunLocation rl = customRunLocations.get(currentLocationIndex);
        if (rl.getPatchTypes() != null && currentPatchTypeIndex + 1 < rl.getPatchTypes().size()) {
            currentPatchTypeIndex++;
            resetPatchStates();
            plugin.clearLastMessage();
        } else {
            moveToNextLocation();
        }
    }

    private void moveToNextLocation() {
        startSubCases = false;
        currentLocationIndex++;
        currentPatchTypeIndex = 0;
        farmLimps = false;
        resetPatchStates();
        plugin.clearLastMessage();
        navigationHandler.currentTeleportCase = 1;
        navigationHandler.isAtDestination = false;
        if (currentLocationIndex >= customRunLocations.size()) removeOverlay();
    }

    private void resetPatchStates() { farmingStepHandler.resetCompostStates(); }

    public void removeOverlay() {
        plugin.clearLastMessage();
        plugin.setItemsCollected(false); // FORCES STEP 1 ON NEXT RUN

        plugin.overlayManager.remove(farmingHelperOverlay);
        plugin.overlayManager.remove(this);
        plugin.overlayManager.remove(farmingHelperOverlayInfoBox);
        plugin.setOverlayActive(false);
        plugin.setTeleportOverlayActive(false);

        customRunMode = false;
        activeCustomRunName = null;
        currentPatchTypeIndex = 0;
        currentLocationIndex = 0;
        startSubCases = false;
        farmLimps = false;

        resetPatchStates();
        farmingStepHandler.clearHintArrow();
        navigationHandler.currentTeleportCase = 1;
        navigationHandler.isAtDestination = false;

        SwingUtilities.invokeLater(() -> { if (plugin.panel != null) plugin.panel.onCustomRunEnded(); });
    }

    public void startCustomRun(CustomRun run) {
        if (run == null || run.getLocations() == null) return;

        plugin.setCustomRunToolInclusion(run.isIncludeSecateurs(), run.isIncludeDibber(), run.isIncludeRake());
        plugin.setItemsCollected(false); // FORCES STEP 1 IMMEDIATELY
        plugin.clearLastMessage();

        resetPatchStates();
        farmingStepHandler.clearHintArrow();

        customRunMode = true;
        activeCustomRunName = run.getName();
        customRunLocations = new ArrayList<>(run.getLocations());

        currentLocationIndex = 0;
        currentPatchTypeIndex = 0;
        startSubCases = false;
        farmLimps = false;

        navigationHandler.currentTeleportCase = 1;
        navigationHandler.isAtDestination = false;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.isTeleportOverlayActive() || client.getLocalPlayer() == null || customRunLocations.isEmpty()) return null;
        if (navigationHandler.isAtDestination && startSubCases) handleCustomRunSteps(graphics);
        else navigateToCurrentLocation(graphics);
        return null;
    }
}