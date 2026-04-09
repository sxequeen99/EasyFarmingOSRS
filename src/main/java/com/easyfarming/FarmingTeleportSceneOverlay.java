package com.easyfarming;

import java.awt.*;
import javax.inject.Inject;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import com.easyfarming.overlays.highlighting.GameObjectHighlighter;
import com.easyfarming.utils.Constants;

public class FarmingTeleportSceneOverlay extends Overlay {
    private final GameObjectHighlighter gameObjectHighlighter;

    private volatile Color spiritTreeHighlightColor = null;
    private volatile Color fossilIslandHighlightColor = null;

    @Inject
    public FarmingTeleportSceneOverlay(GameObjectHighlighter gameObjectHighlighter) {
        this.gameObjectHighlighter = gameObjectHighlighter;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public void requestSpiritTreeHighlight(Color color) {
        this.spiritTreeHighlightColor = color;
    }

    public void clearSpiritTreeHighlight() {
        this.spiritTreeHighlightColor = null;
    }

    public void requestFossilIslandHighlight(Color color) {
        this.fossilIslandHighlightColor = color;
    }

    public void clearFossilIslandHighlight() {
        this.fossilIslandHighlightColor = null;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (spiritTreeHighlightColor != null) {
            for (Integer objectId : Constants.SPIRIT_TREE_IDS) {
                // Uses the new, safe render method
                gameObjectHighlighter.renderHighlight(graphics, objectId, spiritTreeHighlightColor);
            }
        }

        if (fossilIslandHighlightColor != null) {
            for (Integer objectId : Constants.BIRDHOUSE_PATCH_IDS) {
                gameObjectHighlighter.renderHighlight(graphics, objectId, fossilIslandHighlightColor);
            }

            for (Integer objectId : Constants.MAGIC_MUSHTREE_IDS) {
                gameObjectHighlighter.renderHighlight(graphics, objectId, fossilIslandHighlightColor);
            }
        }

        return null;
    }
}