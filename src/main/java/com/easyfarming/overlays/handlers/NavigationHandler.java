package com.easyfarming.overlays.handlers;

import com.easyfarming.*;
import com.easyfarming.core.Location;
import com.easyfarming.core.Teleport;
import com.easyfarming.overlays.highlighting.*;
import com.easyfarming.overlays.utils.ColorProvider;
import com.easyfarming.overlays.utils.GameObjectHelper;
import com.easyfarming.overlays.utils.WidgetHelper;
import com.easyfarming.utils.Constants;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;

import javax.inject.Inject;
import java.awt.*;

public class NavigationHandler {
    private final Client client;
    private final EasyFarmingPlugin plugin;
    private final EasyFarmingConfig config;
    private final AreaCheck areaCheck;
    private final TeleportHighlighter teleportHighlighter;
    private final PatchHighlighter patchHighlighter;
    private final ItemHighlighter itemHighlighter;
    private final WidgetHighlighter widgetHighlighter;
    private final GameObjectHighlighter gameObjectHighlighter;
    private final DecorativeObjectHighlighter decorativeObjectHighlighter;
    private final MenuHighlighter menuHighlighter;
    private final WidgetHelper widgetHelper;
    private final GameObjectHelper gameObjectHelper;
    private final ColorProvider colorProvider;
    private final FarmingTeleportSceneOverlay farmingTeleportSceneOverlay;

    public int currentTeleportCase = 1;
    public boolean isAtDestination = false;
    public boolean teleportHandled = false; // The State Lock variable

    @Inject
    public NavigationHandler(Client client, EasyFarmingPlugin plugin, EasyFarmingConfig config,
                             AreaCheck areaCheck, TeleportHighlighter teleportHighlighter,
                             PatchHighlighter patchHighlighter, ItemHighlighter itemHighlighter,
                             WidgetHighlighter widgetHighlighter, GameObjectHighlighter gameObjectHighlighter,
                             DecorativeObjectHighlighter decorativeObjectHighlighter, MenuHighlighter menuHighlighter,
                             WidgetHelper widgetHelper, GameObjectHelper gameObjectHelper,
                             ColorProvider colorProvider, FarmingTeleportSceneOverlay farmingTeleportSceneOverlay) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.areaCheck = areaCheck;
        this.teleportHighlighter = teleportHighlighter;
        this.patchHighlighter = patchHighlighter;
        this.itemHighlighter = itemHighlighter;
        this.widgetHighlighter = widgetHighlighter;
        this.gameObjectHighlighter = gameObjectHighlighter;
        this.decorativeObjectHighlighter = decorativeObjectHighlighter;
        this.menuHighlighter = menuHighlighter;
        this.widgetHelper = widgetHelper;
        this.gameObjectHelper = gameObjectHelper;
        this.colorProvider = colorProvider;
        this.farmingTeleportSceneOverlay = farmingTeleportSceneOverlay;
    }

    public void inHouseCheck() {
        if (gameObjectHelper.getGameObjectIdsByName("Portal").contains(4525)) {
            this.currentTeleportCase = 2;
        }
    }

    public void gettingToHouse(Graphics2D graphics) {
        EasyFarmingConfig.OptionEnumHouseTele teleportOption = config.enumConfigHouseTele();
        Color leftColor = colorProvider.getLeftClickColorWithAlpha();
        Color rightColor = colorProvider.getRightClickColorWithAlpha();

        switch (teleportOption) {
            case Law_air_earth_runes:
                InventoryTabChecker.TabState tabState = InventoryTabChecker.checkTab(client, VarClientID.TOPLEVEL_PANEL);
                switch (tabState) {
                    case INVENTORY:
                    case REST: widgetHighlighter.interfaceOverlay(widgetHelper.getSpellbookIconGroupId(), widgetHelper.getSpellbookIconChildId()).render(graphics); break;
                    case SPELLBOOK: widgetHighlighter.interfaceOverlay(InterfaceID.MAGIC_SPELLBOOK, 32).render(graphics); inHouseCheck(); break;
                }
                break;
            case Teleport_To_House: inHouseCheck(); itemHighlighter.itemHighlight(graphics, ItemID.POH_TABLET_TELEPORTTOHOUSE, leftColor); break;
            case Construction_cape: inHouseCheck(); itemHighlighter.itemHighlight(graphics, ItemID.SKILLCAPE_CONSTRUCTION, rightColor); break;
            case Construction_cape_t: inHouseCheck(); itemHighlighter.itemHighlight(graphics, ItemID.SKILLCAPE_CONSTRUCTION_TRIMMED, rightColor); break;
            case Max_cape: inHouseCheck(); itemHighlighter.itemHighlight(graphics, ItemID.SKILLCAPE_MAX, rightColor); break;
        }
    }

    public boolean shouldProceedToFarming(Location location, Teleport teleport) {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null || teleport == null) return false;

        WorldPoint targetLocation = teleport.getPoint();
        int currentRegionId = localPlayer.getWorldLocation().getRegionID();

        // 1. UNIVERSAL ARRIVAL CHECK: The teleport object natively knows the exact coordinates of the patch!
        if (targetLocation != null && areaCheck.isPlayerWithinArea(targetLocation, 20)) {
            return true;
        }

        // 2. BACKUP CHECK: Fossil Island & Birdhouses (which sometimes lack a specific point)
        if (location.getName() != null && (location.getName().contains("Fossil Island") || location.getName().contains("Birdhouse"))) {
            return currentRegionId == 14908 || currentRegionId == 14906 || currentRegionId == 14652 || currentRegionId == 14651 || currentRegionId == 15162 || currentRegionId == 15164;
        }

        return false;
    }

    public void adaptiveHighlighting(Location location, Teleport teleport, Graphics2D graphics, String patchType) {
        if (client.getLocalPlayer() == null || teleport.getPoint() == null) return;
        int currentRegionId = client.getLocalPlayer().getWorldLocation().getRegionID();

        // --- THIS KILL-SWITCH FIXES THE WHISTLE HIGHLIGHT BUG ---
        boolean inLandingZone = ("Auburnvale".equals(location.getName()) && currentRegionId == 5684) ||
                ("Civitas illa Fortis".equals(location.getName()) && currentRegionId == 6459) ||
                ("Kastori".equals(location.getName()) && (currentRegionId == 5167 || currentRegionId == 5423));
        if (inLandingZone) return;

        teleportHighlighter.highlightTeleportMethod(teleport, graphics);
    }

    public void gettingToLocation(Graphics2D graphics, Location location, String patchType) {
        Teleport teleport = location.getSelectedTeleport(patchType);
        if (teleport == null || isAtDestination || client.getLocalPlayer() == null) return;

        int currentRegionId = client.getLocalPlayer().getWorldLocation().getRegionID();
        WorldPoint targetLocation = teleport.getPoint();

        // STATE 3: Arrived at Patch (Handoff to FarmingStepHandler)
        if (shouldProceedToFarming(location, teleport)) {
            this.currentTeleportCase = 1;
            this.teleportHandled = false; // Reset lock for safety
            isAtDestination = true;
            plugin.clearLastMessage();
            return;
        }

        // STATE LOCK TRIGGER: Shrink radius to 40 so Catherby/Falador don't overlap, but Portal Nexuses still work
        boolean isCloseToPatch = (targetLocation != null && areaCheck.isPlayerWithinArea(targetLocation, 40));
        if (!teleportHandled && (currentRegionId == teleport.getRegionId() || isCloseToPatch)) {
            this.currentTeleportCase = 1;
            teleportHandled = true;
        }

        // STATE 2: Navigating (Teleport done, we are running)
        if (teleportHandled) {
            plugin.addTextToInfoBox("Run to the " + location.getName() + " patch!");
            return; // Lock the UI. Ignore region boundaries and stop drawing teleport highlights.
        }

        // STATE 1: Needs Teleport
        plugin.addTextToInfoBox(teleport.getDescription());
        adaptiveHighlighting(location, teleport, graphics, patchType);

        switch (teleport.getCategory()) {
            case ITEM: handleItemTeleport(graphics, teleport, location, currentRegionId); break;
            case PORTAL_NEXUS: handlePortalNexusTeleport(graphics, teleport, location, currentRegionId); break;
            case SPIRIT_TREE: handleSpiritTreeTeleport(graphics, teleport, location, currentRegionId); break;
            case FAIRY_RING: handleFairyRingTeleport(graphics, teleport, location, currentRegionId); break;
            case JEWELLERY_BOX: handleJewelleryBoxTeleport(graphics, teleport, location, currentRegionId); break;
            case MOUNTED_XERICS: handleMountedXericsTeleport(graphics, teleport, location, currentRegionId); break;
            case SPELLBOOK: handleSpellbookTeleport(graphics, teleport, currentRegionId); break;
        }
    }

    private void handleItemTeleport(Graphics2D graphics, Teleport teleport, Location location, int currentRegionId) {
        Color leftColor = colorProvider.getLeftClickColorWithAlpha();
        Color rightColor = colorProvider.getRightClickColorWithAlpha();

        boolean inLandingZone = ("Auburnvale".equals(location.getName()) && currentRegionId == 5684) ||
                ("Civitas illa Fortis".equals(location.getName()) && currentRegionId == 6459) ||
                ("Kastori".equals(location.getName()) && (currentRegionId == 5167 || currentRegionId == 5423));

        if (!inLandingZone) {
            if (teleport.getInterfaceGroupId() != 0) {
                if (!widgetHelper.isInterfaceOpen(teleport.getInterfaceGroupId(), teleport.getInterfaceChildId())) {
                    itemHighlighter.itemHighlight(graphics, teleport.getId(), rightColor);
                    if (!teleport.getRightClickOption().equals("")) menuHighlighter.highlightRightClickOption(graphics, teleport.getRightClickOption());
                } else {
                    Widget widget = client.getWidget(teleport.getInterfaceGroupId(), teleport.getInterfaceChildId());
                    widgetHighlighter.highlightDynamicComponent(graphics, widget, 1);
                }
            } else {
                if (!teleport.getRightClickOption().equals("")) {
                    itemHighlighter.itemHighlight(graphics, teleport.getId(), rightColor);
                    menuHighlighter.highlightRightClickOption(graphics, teleport.getRightClickOption());
                } else {
                    if (plugin.getEasyFarmingOverlay().isTeleportCrystal(teleport.getId())) itemHighlighter.highlightTeleportCrystal(graphics);
                    else if (plugin.getEasyFarmingOverlay().isQuetzalWhistle(teleport.getId()) || plugin.getEasyFarmingOverlay().isRoyalSeedPod(teleport.getId()) || plugin.getEasyFarmingOverlay().isEctophial(teleport.getId())) itemHighlighter.itemHighlight(graphics, teleport.getId(), leftColor);
                    else itemHighlighter.itemHighlight(graphics, teleport.getId(), leftColor);
                }
            }
        }
    }

    private void handlePortalNexusTeleport(Graphics2D graphics, Teleport teleport, Location location, int currentRegionId) {
        Color leftColor = colorProvider.getLeftClickColorWithAlpha();
        switch (this.currentTeleportCase) {
            case 1: gettingToHouse(graphics); break;
            case 2:
                if (!widgetHelper.isInterfaceOpen(17, 0)) {
                    for (Integer objectId : gameObjectHelper.getGameObjectIdsByName("Portal Nexus")) gameObjectHighlighter.renderHighlight(graphics, objectId, leftColor);
                } else {
                    Widget widget = client.getWidget(Constants.INTERFACE_PORTAL_NEXUS, Constants.INTERFACE_PORTAL_NEXUS_CHILD);
                    widgetHighlighter.highlightDynamicComponent(graphics, widget, widgetHelper.getChildIndexPortalNexus(location.getName()));
                }
                break;
        }
    }

    private void handleSpiritTreeTeleport(Graphics2D graphics, Teleport teleport, Location location, int currentRegionId) {
        Color leftColor = colorProvider.getLeftClickColorWithAlpha();
        if (!widgetHelper.isInterfaceOpen(187, 3)) {
            for (Integer objectId : Constants.SPIRIT_TREE_IDS) gameObjectHighlighter.renderHighlight(graphics, objectId, leftColor);
        } else {
            Widget widget = client.getWidget(Constants.INTERFACE_SPIRIT_TREE, Constants.INTERFACE_SPIRIT_TREE_CHILD);
            switch (location.getName()) {
                case "Gnome Stronghold": widgetHighlighter.highlightDynamicComponent(graphics, widget, widgetHelper.getChildIndexSpiritTree("Gnome Stronghold")); break;
                case "Tree Gnome Village": widgetHighlighter.highlightDynamicComponent(graphics, widget, widgetHelper.getChildIndexSpiritTree("Tree Gnome Village")); break;
                case "Falador": widgetHighlighter.highlightDynamicComponent(graphics, widget, widgetHelper.getChildIndexSpiritTree("Port Sarim")); break;
                case "Kourend": widgetHighlighter.highlightDynamicComponent(graphics, widget, widgetHelper.getChildIndexSpiritTree("Hosidius")); break;
                case "Farming Guild": widgetHighlighter.highlightDynamicComponent(graphics, widget, widgetHelper.getChildIndexSpiritTree("Farming Guild")); break;
            }
        }
    }

    private void handleJewelleryBoxTeleport(Graphics2D graphics, Teleport teleport, Location location, int currentRegionId) {
        Color leftColor = colorProvider.getLeftClickColorWithAlpha();
        switch (this.currentTeleportCase) {
            case 1: gettingToHouse(graphics); break;
            case 2:
                if (!widgetHelper.isInterfaceOpen(Constants.INTERFACE_JEWELLERY_BOX_OPEN, 0)) {
                    for (int id : Constants.JEWELLERY_BOX_IDS) gameObjectHighlighter.renderHighlight(graphics, id, leftColor);
                    gameObjectHighlighter.renderHighlight(graphics, teleport.getId(), leftColor);
                } else {
                    Widget widget = client.getWidget(Constants.INTERFACE_JEWELLERY_BOX_OPEN, Constants.WIDGET_JEWELLERY_BOX_CHILD);
                    widgetHighlighter.highlightDynamicComponent(graphics, widget, 10);
                }
                break;
        }
    }

    private void handleMountedXericsTeleport(Graphics2D graphics, Teleport teleport, Location location, int currentRegionId) {
        switch (this.currentTeleportCase) {
            case 1: gettingToHouse(graphics); break;
            case 2:
                Color leftColor = colorProvider.getLeftClickColorWithAlpha();
                if (!widgetHelper.isInterfaceOpen(teleport.getInterfaceGroupId(), teleport.getInterfaceChildId())) {
                    for (int id : Constants.XERICS_TALISMAN_IDS) decorativeObjectHighlighter.highlightDecorativeObject(id, leftColor).render(graphics);
                } else {
                    Widget widget = client.getWidget(teleport.getInterfaceGroupId(), teleport.getInterfaceChildId());
                    widgetHighlighter.highlightDynamicComponent(graphics, widget, 1);
                }
                break;
        }
    }

    private void handleFairyRingTeleport(Graphics2D graphics, Teleport teleport, Location location, int currentRegionId) {
        Color leftColor = colorProvider.getLeftClickColorWithAlpha();
        gameObjectHighlighter.renderHighlight(graphics, Constants.FAIRY_RING_OBJECT_ID, leftColor);
    }

    private void handleSpellbookTeleport(Graphics2D graphics, Teleport teleport, int currentRegionId) {
        InventoryTabChecker.TabState tabState = InventoryTabChecker.checkTab(client, VarClientID.TOPLEVEL_PANEL);
        switch (tabState) {
            case REST:
            case INVENTORY:
                widgetHighlighter.interfaceOverlay(widgetHelper.getSpellbookIconGroupId(), widgetHelper.getSpellbookIconChildId()).render(graphics);
                break;
            case SPELLBOOK:
                widgetHighlighter.interfaceOverlay(teleport.getInterfaceGroupId(), teleport.getInterfaceChildId()).render(graphics);
                break;
        }
    }
}