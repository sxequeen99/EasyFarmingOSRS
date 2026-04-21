package com.easyfarming.overlays.highlighting;

import com.easyfarming.EasyFarmingOverlay;
import com.easyfarming.customrun.PatchTypes;
import com.easyfarming.utils.Constants;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.client.ui.overlay.Overlay;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;

public class PatchHighlighter {
    private final Client client;
    private final EasyFarmingOverlay farmingHelperOverlay;
    private final GameObjectHighlighter gameObjectHighlighter;

    @Inject
    public PatchHighlighter(Client client, EasyFarmingOverlay farmingHelperOverlay, GameObjectHighlighter gameObjectHighlighter) {
        this.client = client;
        this.farmingHelperOverlay = farmingHelperOverlay;
        this.gameObjectHighlighter = gameObjectHighlighter;
    }

    public void highlightHerbPatches(Graphics2D graphics, Color color) {
        for (Integer patchId : farmingHelperOverlay.getHerbPatchIds()) {
            gameObjectHighlighter.renderHighlight(graphics, patchId, color);
        }
    }

    public void highlightSpecificHerbPatch(Graphics2D graphics, int objectId, Color color) {
        gameObjectHighlighter.renderHighlight(graphics, objectId, color);
    }

    public void highlightFlowerPatches(Graphics2D graphics, Color color) {
        for (Integer patchId : farmingHelperOverlay.getFlowerPatchIds()) {
            gameObjectHighlighter.renderHighlight(graphics, patchId, color);
        }
    }

    public void highlightAllotmentPatches(Graphics2D graphics, Color color) {
        for (List<Integer> patchIds : Constants.ALLOTMENT_PATCH_IDS_BY_LOCATION.values()) {
            for (Integer patchId : patchIds) {
                if (patchId == null) continue;
                gameObjectHighlighter.renderHighlight(graphics, patchId, color);
            }
        }
    }

    public void highlightSpecificAllotmentPatch(Graphics2D graphics, int objectId, Color color) {
        gameObjectHighlighter.renderHighlight(graphics, objectId, color);
    }

    public void highlightTreePatches(Graphics2D graphics, Color color) {
        for (Integer patchId : farmingHelperOverlay.getTreePatchIds()) {
            gameObjectHighlighter.renderHighlight(graphics, patchId, color);
        }
    }

    public void highlightFruitTreePatches(Graphics2D graphics, Color color) {
        for (Integer patchId : farmingHelperOverlay.getFruitTreePatchIds()) {
            gameObjectHighlighter.renderHighlight(graphics, patchId, color);
        }
    }

    public void highlightHopsPatches(Graphics2D graphics, Color color) {
        for (Integer patchId : farmingHelperOverlay.getHopsPatchIds()) {
            gameObjectHighlighter.renderHighlight(graphics, patchId, color);
        }
    }

    public void highlightBirdhousePatches(Graphics2D graphics, Color defaultColor, boolean vnDone, boolean vsDone, boolean mnDone, boolean msDone) {
        if (client.getLocalPlayer() == null) return;
        int regionId = client.getLocalPlayer().getWorldLocation().getRegionID();

        boolean valleyDone = vnDone && vsDone;

        // Custom Colors
        Color doneColor = new Color(0, 255, 0, 150);       // Green for finished
        Color unseededColor = new Color(255, 0, 0, 150);   // Red for unseeded/empty

        for (Integer patchId : Constants.BIRDHOUSE_PATCH_IDS) {

            // ALWAYS highlight the Mushtree so you can teleport around (Keeps default blue)
            if (patchId == 30920) {
                gameObjectHighlighter.renderHighlight(graphics, patchId, defaultColor);
                continue;
            }

            boolean isAnyBuiltHouse = (patchId >= 30571 && patchId <= 30597);

            // STEP 1: Verdant Valley
            if (regionId == 14906) {
                if (patchId == 30567 || patchId == 30568 || isAnyBuiltHouse) {
                    Color c = valleyDone ? doneColor : unseededColor;
                    gameObjectHighlighter.renderHighlight(graphics, patchId, c);
                }
            }
            // STEP 2: Meadow North
            else if (regionId == 14652 && valleyDone) {
                if (patchId == 30565 || isAnyBuiltHouse) {
                    Color c = mnDone ? doneColor : unseededColor;
                    gameObjectHighlighter.renderHighlight(graphics, patchId, c);
                }
            }
            // STEP 3: Meadow South
            else if (regionId == 14651 && valleyDone && mnDone) {
                if (patchId == 30566 || isAnyBuiltHouse) {
                    Color c = msDone ? doneColor : unseededColor;
                    gameObjectHighlighter.renderHighlight(graphics, patchId, c);
                }
            }
        }
    }

    public void highlightSpecificHopsPatch(Graphics2D graphics, int objectId, Color color) {
        gameObjectHighlighter.renderHighlight(graphics, objectId, color);
    }

    public void highlightSpecificFlowerPatch(Graphics2D graphics, int objectId, Color color) {
        gameObjectHighlighter.renderHighlight(graphics, objectId, color);
    }

    public void highlightFarmingPatchesForLocation(String locationName, Graphics2D graphics,
                                                   String patchType,
                                                   Color leftClickColor, Color useItemColor) {
        if (patchType == null) return;
        switch (patchType) {
            case PatchTypes.HERB:
                if (isHerbLocation(locationName)) highlightHerbPatches(graphics, leftClickColor);
                break;
            case PatchTypes.FLOWER:
                if (isHerbLocation(locationName)) highlightFlowerPatches(graphics, leftClickColor);
                break;
            case PatchTypes.ALLOTMENT:
                if (isHerbLocation(locationName)) highlightAllotmentPatches(graphics, leftClickColor);
                break;
            case PatchTypes.TREE:
                if (isTreeLocation(locationName)) highlightTreePatches(graphics, leftClickColor);
                break;
            case PatchTypes.FRUIT_TREE:
                if (isFruitTreeLocation(locationName)) highlightFruitTreePatches(graphics, leftClickColor);
                break;
            case PatchTypes.HOPS:
                if (isHopsLocation(locationName)) highlightHopsPatches(graphics, leftClickColor);
                break;
            case "BIRDHOUSE":
                // Feed it false flags while navigating so it defaults to standard behavior before arrival
                if (isBirdhouseLocation(locationName)) highlightBirdhousePatches(graphics, leftClickColor, false, false, false, false);
                break;
        }
    }

    private static boolean isHerbLocation(String locationName) {
        return locationName.equals("Ardougne") || locationName.equals("Catherby")
                || locationName.equals("Falador") || locationName.equals("Farming Guild")
                || locationName.equals("Harmony Island") || locationName.equals("Kourend")
                || locationName.equals("Morytania") || locationName.equals("Troll Stronghold")
                || locationName.equals("Weiss") || locationName.equals("Civitas illa Fortis");
    }

    private static boolean isBirdhouseLocation(String locationName) {
        return locationName.equals("Fossil Island");
    }

    private static boolean isTreeLocation(String locationName) {
        return locationName.equals("Falador") || locationName.equals("Farming Guild")
                || locationName.equals("Gnome Stronghold") || locationName.equals("Lumbridge")
                || locationName.equals("Taverley") || locationName.equals("Varrock")
                || locationName.equals("Auburnvale");
    }

    private static boolean isFruitTreeLocation(String locationName) {
        return locationName.equals("Brimhaven") || locationName.equals("Catherby")
                || locationName.equals("Farming Guild") || locationName.equals("Gnome Stronghold")
                || locationName.equals("Lletya") || locationName.equals("Tree Gnome Village")
                || locationName.equals("Kastori");
    }

    private static boolean isHopsLocation(String locationName) {
        return locationName.equals("Lumbridge") || locationName.equals("Seers Village")
                || locationName.equals("Yanille") || locationName.equals("Entrana")
                || locationName.equals("Aldarin");
    }
}