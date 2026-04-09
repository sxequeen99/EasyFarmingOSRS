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

/**
 * Handles highlighting of farming patches (herb, flower, tree, fruit tree).
 */
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

    /**
     * Highlights a specific herb patch by object ID (for current location).
     */
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

    /**
     * Highlights a specific allotment patch by object ID.
     * @param graphics Graphics context
     * @param objectId The object ID of the specific patch to highlight
     * @param color The color to use for highlighting
     */
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
    public void highlightBirdhousePatches(Graphics2D graphics, Color color) {
        if (client.getLocalPlayer() == null) return;

        net.runelite.api.coords.WorldPoint playerPos = client.getLocalPlayer().getWorldLocation();

        for (Integer patchId : Constants.BIRDHOUSE_PATCH_IDS) {

            // Check if this ID is ANY built birdhouse (Regular to Redwood)
            boolean isAnyBuiltHouse = (patchId >= 30571 && patchId <= 30597);

            // 1. ARRIVAL / VALLEY FIRST: North area of Mushroom Meadow
            if (playerPos.getY() > 3780) {
                // Highlight Mushtree, Meadow North Space, or any house built there
                if (patchId == 30920 || patchId == 30565 || isAnyBuiltHouse) {
                    gameObjectHighlighter.renderHighlight(graphics, patchId, color);
                }
            }

            // 2. VERDANT VALLEY: Highlight everything in the Valley
            else if (playerPos.getX() > 3730 && playerPos.getY() < 3750) {
                gameObjectHighlighter.renderHighlight(graphics, patchId, color);
            }

            // 3. MEADOW SOUTH: Only the swamp house area
            else {
                // Highlight Meadow South Space or any house built there
                if (patchId == 30566 || isAnyBuiltHouse) {
                    gameObjectHighlighter.renderHighlight(graphics, patchId, color);
                }
            }
        }
    }

    /**
     * Highlights a specific hops patch by object ID.
     * @param graphics Graphics context
     * @param objectId The object ID of the specific patch to highlight
     * @param color The color to use for highlighting
     */
    public void highlightSpecificHopsPatch(Graphics2D graphics, int objectId, Color color) {
        gameObjectHighlighter.renderHighlight(graphics, objectId, color);
    }

    /**
     * Highlights a specific flower patch by object ID.
     * @param graphics Graphics context
     * @param objectId The object ID of the specific patch to highlight
     * @param color The color to use for highlighting
     */
    public void highlightSpecificFlowerPatch(Graphics2D graphics, int objectId, Color color) {
        gameObjectHighlighter.renderHighlight(graphics, objectId, color);
    }

    /**
     * Highlights farming patches for a specific location based on patch type.
     * @param locationName The name of the location
     * @param graphics Graphics context for highlighting
     * @param patchType One of PatchTypes.HERB, FLOWER, ALLOTMENT, TREE, FRUIT_TREE, HOPS
     * @param leftClickColor Color for left-click highlights
     * @param useItemColor Color for use-item highlights
     */
    public void highlightFarmingPatchesForLocation(String locationName, Graphics2D graphics,
                                                   String patchType,
                                                   Color leftClickColor, Color useItemColor) {
        if (patchType == null) {
            return;
        }
        switch (patchType) {
            case PatchTypes.HERB:
                if (isHerbLocation(locationName)) {
                    highlightHerbPatches(graphics, leftClickColor);
                }
                break;
            case PatchTypes.FLOWER:
                if (isHerbLocation(locationName)) {
                    highlightFlowerPatches(graphics, leftClickColor);
                }
                break;
            case PatchTypes.ALLOTMENT:
                if (isHerbLocation(locationName)) {
                    highlightAllotmentPatches(graphics, leftClickColor);
                }
                break;
            case PatchTypes.TREE:
                if (isTreeLocation(locationName)) {
                    highlightTreePatches(graphics, leftClickColor);
                }
                break;
            case PatchTypes.FRUIT_TREE:
                if (isFruitTreeLocation(locationName)) {
                    highlightFruitTreePatches(graphics, leftClickColor);
                }
                break;
            case PatchTypes.HOPS:
                if (isHopsLocation(locationName)) {
                    highlightHopsPatches(graphics, leftClickColor);
                }
                break;
            case "BIRDHOUSE":
                if (isBirdhouseLocation(locationName)) { // Use the helper here!
                    highlightBirdhousePatches(graphics, leftClickColor);
                }
                break;
            default:
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
                || locationName.equals("Taverley") || locationName.equals("Varrock");
    }

    private static boolean isFruitTreeLocation(String locationName) {
        return locationName.equals("Brimhaven") || locationName.equals("Catherby")
                || locationName.equals("Farming Guild") || locationName.equals("Gnome Stronghold")
                || locationName.equals("Lletya") || locationName.equals("Tree Gnome Village");
    }

    private static boolean isHopsLocation(String locationName) {
        return locationName.equals("Lumbridge") || locationName.equals("Seers Village")
                || locationName.equals("Yanille") || locationName.equals("Entrana")
                || locationName.equals("Aldarin");
    }
}