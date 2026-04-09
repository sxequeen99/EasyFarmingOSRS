package com.easyfarming.ui;

import com.easyfarming.EasyFarmingPlugin;
import com.easyfarming.customrun.LocationCatalog;
import com.easyfarming.customrun.PatchTypes;
import com.easyfarming.customrun.RunLocation;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.game.ItemManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ultra-compact location sub-panel: location name header (draggable) and patch icons.
 * Automatically limits height to prevent UI stretching.
 */
public class CustomRunLocationSubPanel extends JPanel {
    private static final int PATCH_ICON_SIZE = 36;
    private static final int GRIMY_RANARR_WEED = 207;
    private static final int BIRD_HOUSE = 22204;

    private final EasyFarmingPlugin plugin;
    private final ItemManager itemManager;
    private final String locationName;
    private final RunLocation runLocation;
    private final Runnable onChanged;

    private final JPanel patchIconsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    private boolean refreshingFromRun = false;

    public CustomRunLocationSubPanel(EasyFarmingPlugin plugin, ItemManager itemManager, String locationName,
                                     RunLocation runLocation, Runnable onChanged) {
        this(plugin, itemManager, locationName, runLocation, onChanged, null);
    }

    public CustomRunLocationSubPanel(EasyFarmingPlugin plugin, ItemManager itemManager, String locationName,
                                     RunLocation runLocation, Runnable onChanged,
                                     java.util.function.Consumer<String> onDragStart) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.locationName = locationName;
        this.runLocation = runLocation;
        this.onChanged = onChanged;

        runLocation.setLocationName(locationName);

        // 1. Sets the layout to stack vertically without stretching
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ColorScheme.DARK_GRAY_COLOR, 1),
                new EmptyBorder(3, 10, 3, 10))); // Tight padding

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Forces it to the left

        if (onDragStart != null) {
            JLabel gripLabel = new JLabel("\u22EE");
            gripLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
            gripLabel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
            gripLabel.setToolTipText("Drag to reorder");
            gripLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
                    onDragStart.accept(locationName);
                }
            });
            headerPanel.add(gripLabel);
        }

        JLabel nameLabel = new JLabel(locationName);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(FontManager.getRunescapeBoldFont());
        if (onDragStart != null) {
            nameLabel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
            nameLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    onDragStart.accept(locationName);
                }
            });
        }
        headerPanel.add(nameLabel);
        add(headerPanel);

        patchIconsPanel.setOpaque(false);
        patchIconsPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Forces icons to the left
        add(patchIconsPanel);

        refreshPatchIcons();
    }

    public String getLocationName() {
        return locationName;
    }

    private void refreshTeleportOptions() {
        LocationCatalog catalog = plugin.getLocationCatalog();
        List<String> opts = catalog.getTeleportOptionsForLocation(locationName);

        // Ensures a teleport is selected in the background without a UI dropdown
        String current = runLocation.getTeleportOption();
        if (current == null || !opts.contains(current)) {
            if (!opts.isEmpty()) {
                runLocation.setTeleportOption(opts.get(0));
            }
        }
    }

    private void refreshPatchIcons() {
        patchIconsPanel.removeAll();
        LocationCatalog catalog = plugin.getLocationCatalog();
        List<String> available = catalog.getPatchTypesAtLocation(locationName);
        List<String> selected = runLocation.getPatchTypes() != null ? runLocation.getPatchTypes() : new ArrayList<>();
        for (String patchType : available) {
            JButton iconBtn = makePatchIconButton(patchType, selected.contains(patchType));
            patchIconsPanel.add(iconBtn);
        }
        patchIconsPanel.revalidate();
        patchIconsPanel.repaint();
    }

    public void refreshFromRunLocation() {
        refreshingFromRun = true;
        try {
            setBackground(ColorScheme.DARKER_GRAY_COLOR);
            refreshTeleportOptions();
            refreshPatchIcons();
        } finally {
            refreshingFromRun = false;
        }
    }

    private JButton makePatchIconButton(String patchType, boolean selected) {
        int itemId = itemIdForPatchType(patchType);
        String tooltip = displayName(patchType);
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(PATCH_ICON_SIZE, PATCH_ICON_SIZE));
        btn.setFocusable(false);
        btn.setToolTipText(tooltip);
        btn.setBackground(selected ? new Color(30, 60, 30) : ColorScheme.DARKER_GRAY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        if (itemManager != null) {
            itemManager.getImage(itemId).addTo(btn);
        } else {
            btn.setText(tooltip);
        }
        btn.addActionListener(e -> {
            List<String> types = runLocation.getPatchTypes();
            if (types == null) {
                types = new ArrayList<>();
                runLocation.setPatchTypes(types);
            }
            if (types.contains(patchType)) {
                types.remove(patchType);
            } else {
                types.add(patchType);
            }
            btn.setBackground(types.contains(patchType) ? new Color(30, 60, 30) : ColorScheme.DARKER_GRAY_COLOR);
            if (onChanged != null) onChanged.run();
        });
        return btn;
    }

    private static int itemIdForPatchType(String patchType) {
        switch (patchType) {
            case PatchTypes.HERB: return GRIMY_RANARR_WEED;
            case PatchTypes.FLOWER: return net.runelite.api.gameval.ItemID.LIMPWURT_ROOT;
            case PatchTypes.ALLOTMENT: return net.runelite.api.gameval.ItemID.WATERMELON;
            case PatchTypes.TREE: return net.runelite.api.gameval.ItemID.YEW_LOGS;
            case PatchTypes.FRUIT_TREE: return net.runelite.api.gameval.ItemID.PINEAPPLE;
            case PatchTypes.HOPS: return net.runelite.api.gameval.ItemID.BARLEY;
            case PatchTypes.BIRD_HOUSE: return BIRD_HOUSE;
            default: return GRIMY_RANARR_WEED;
        }
    }

    private static String displayName(String patchType) {
        switch (patchType) {
            case PatchTypes.HERB: return "Herb";
            case PatchTypes.FLOWER: return "Flower";
            case PatchTypes.ALLOTMENT: return "Allotment";
            case PatchTypes.TREE: return "Tree";
            case PatchTypes.FRUIT_TREE: return "Fruit tree";
            case PatchTypes.HOPS: return "Hops";
            case PatchTypes.BIRD_HOUSE: return "Bird House";
            default: return patchType.replace("_", " ");
        }
    }

    // 2. This completely prevents the UI from stretching vertically
    @Override
    public Dimension getMaximumSize() {
        Dimension preferred = getPreferredSize();
        return new Dimension(super.getMaximumSize().width, preferred.height);
    }
}