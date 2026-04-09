package com.easyfarming;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.ColorScheme;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;

public class EasyFarmingOverlayInfoBox extends OverlayPanel {
    private final Client client;
    private final EasyFarmingPlugin plugin;

    private String text;
    private String debugText;
    private List<String> missingItemsList; // NEW: Holds the live-updating items

    @Inject
    public EasyFarmingOverlayInfoBox(Client client, EasyFarmingPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);

        // --- THE WIDTH FIX ---
        // Forces the box to be wider so your item list doesn't wrap into two lines
        getPanelComponent().setPreferredSize(new Dimension(220, 0));
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDebugText(String debugText) {
        this.debugText = debugText;
    }

    // NEW: Setter for the missing items list
    public void setMissingItemsList(List<String> missingItemsList) {
        this.missingItemsList = missingItemsList;
    }

    // UPDATED: Now clears the list as well
    public void clearText() {
        this.text = null;
        this.debugText = null;
        this.missingItemsList = null;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.isOverlayActive()) {
            return null;
        }

        getPanelComponent().getChildren().clear();

        if (text != null) {
            getPanelComponent().getChildren().add(LineComponent.builder().left(text).build());
        }

        // NEW: Prints the live-updating list right under the main text
        if (missingItemsList != null && !missingItemsList.isEmpty()) {
            for (String item : missingItemsList) {
                getPanelComponent().getChildren().add(LineComponent.builder()
                        .left("  - " + item) // Indented for readability
                        .leftColor(ColorScheme.LIGHT_GRAY_COLOR)
                        .build());
            }
        }

        if (debugText != null) {
            getPanelComponent().getChildren().add(LineComponent.builder().left(debugText).build());
        }

        return super.render(graphics);
    }
}