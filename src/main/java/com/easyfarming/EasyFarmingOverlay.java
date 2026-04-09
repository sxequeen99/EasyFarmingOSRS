package com.easyfarming;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.*;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import java.awt.image.BufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import java.util.Iterator;

import com.easyfarming.customrun.CustomRunItemRequirements;
import com.easyfarming.utils.Constants;

public class EasyFarmingOverlay extends Overlay {

    private final Client client;
    private final EasyFarmingPlugin plugin;
    private final PanelComponent panelComponent = new PanelComponent();
    private final ItemManager itemManager;
    private final InfoBoxManager infoBoxManager;

    // Track current InfoBoxes by item ID
    private final Map<Integer, RequiredItemInfoBox> currentInfoBoxes = new HashMap<>();

    public static final List<Integer> TELEPORT_CRYSTAL_IDS = Arrays.asList(ItemID.MOURNING_TELEPORT_CRYSTAL_1,
            ItemID.MOURNING_TELEPORT_CRYSTAL_2, ItemID.MOURNING_TELEPORT_CRYSTAL_3, ItemID.MOURNING_TELEPORT_CRYSTAL_4,
            ItemID.MOURNING_TELEPORT_CRYSTAL_5, ItemID.PRIF_TELEPORT_CRYSTAL);
    private static final int BASE_TELEPORT_CRYSTAL_ID = ItemID.MOURNING_TELEPORT_CRYSTAL_1;
    /** Lunar staff item ID (satisfies dramen staff for fairy rings); may not be in ItemID in all API versions. */
    private static final int LUNAR_STAFF_ITEM_ID = 9084;

    private static final List<Integer> BIRD_HOUSE_LOG_IDS = Arrays.asList(
            ItemID.LOGS, ItemID.OAK_LOGS, ItemID.WILLOW_LOGS, ItemID.TEAK_LOGS,
            ItemID.MAPLE_LOGS, ItemID.MAHOGANY_LOGS, ItemID.YEW_LOGS,
            ItemID.MAGIC_LOGS, ItemID.REDWOOD_LOGS
    );

    private static final List<Integer> BIRD_HOUSE_SEED_IDS = Arrays.asList(
            ItemID.BARLEY_SEED, ItemID.HAMMERSTONE_HOP_SEED, ItemID.ASGARNIAN_HOP_SEED,
            ItemID.YANILLIAN_HOP_SEED, ItemID.KRANDORIAN_HOP_SEED, ItemID.WILDBLOOD_HOP_SEED,
            ItemID.JUTE_SEED,
            ItemID.POTATO_SEED, ItemID.ONION_SEED, ItemID.CABBAGE_SEED,
            ItemID.TOMATO_SEED, ItemID.SWEETCORN_SEED, ItemID.STRAWBERRY_SEED,
            ItemID.WATERMELON_SEED, ItemID.SNAPE_GRASS_SEED
    );

    public List<Integer> getTeleportCrystalIds() {
        return TELEPORT_CRYSTAL_IDS;
    }

    public boolean isTeleportCrystal(int itemId) {
        return TELEPORT_CRYSTAL_IDS.contains(itemId);
    }

    public static final List<Integer> SKILLS_NECKLACE_IDS = Arrays.asList(ItemID.JEWL_NECKLACE_OF_SKILLS_1,
            ItemID.JEWL_NECKLACE_OF_SKILLS_2, ItemID.JEWL_NECKLACE_OF_SKILLS_3, ItemID.JEWL_NECKLACE_OF_SKILLS_4,
            ItemID.JEWL_NECKLACE_OF_SKILLS_5, ItemID.JEWL_NECKLACE_OF_SKILLS_6);

    // Bottomless compost bucket variants (empty and all filled states)
    private static final List<Integer> BOTTOMLESS_COMPOST_BUCKET_IDS = Arrays.asList(
            ItemID.BOTTOMLESS_COMPOST_BUCKET, // Empty
            22994, // Filled variant 1
            22995, // Filled variant 2
            22996, // Filled variant 3
            22997, // Filled variant 4
            22998 // Filled variant 5
    );

    public List<Integer> getBottomlessCompostBucketIds() {
        return BOTTOMLESS_COMPOST_BUCKET_IDS;
    }

    private static final int BASE_SKILLS_NECKLACE_ID = ItemID.JEWL_NECKLACE_OF_SKILLS_1;

    public List<Integer> getSkillsNecklaceIds() {
        return SKILLS_NECKLACE_IDS;
    }

    public boolean isSkillsNecklace(int itemId) {
        return SKILLS_NECKLACE_IDS.contains(itemId);
    }

    public boolean isDigsitePendant(int itemId) {
        return Constants.DIGSITE_PENDANT_IDS.contains(itemId);
    }

    public static final List<Integer> EXPLORERS_RING_IDS = Arrays.asList(ItemID.LUMBRIDGE_RING_MEDIUM,
            ItemID.LUMBRIDGE_RING_HARD, ItemID.LUMBRIDGE_RING_ELITE);
    private static final int BASE_EXPLORERS_RING_ID = ItemID.LUMBRIDGE_RING_MEDIUM;

    public List<Integer> getExplorersRingIds() {
        return EXPLORERS_RING_IDS;
    }

    public boolean isExplorersRing(int itemId) {
        return EXPLORERS_RING_IDS.contains(itemId);
    }

    public static final List<Integer> ARDY_CLOAK_IDS = Arrays.asList(ItemID.ARDY_CAPE_MEDIUM, ItemID.ARDY_CAPE_HARD,
            ItemID.ARDY_CAPE_ELITE);
    private static final int BASE_ARDY_CLOAK_ID = ItemID.ARDY_CAPE_MEDIUM;

    public List<Integer> getArdyCloakIds() {
        return ARDY_CLOAK_IDS;
    }

    public boolean isArdyCloak(int itemId) {
        return ARDY_CLOAK_IDS.contains(itemId);
    }

    public static final List<Integer> WATERING_CAN_IDS = Constants.WATERING_CAN_IDS;
    private static final int BASE_WATERING_CAN_ID = Constants.WATERING_CAN_IDS.get(0);

    public List<Integer> getWateringCanIds() {
        return WATERING_CAN_IDS;
    }

    public boolean isWateringCan(int itemId) {
        return WATERING_CAN_IDS.contains(itemId);
    }

    public static final List<Integer> COMBAT_BRACELET_IDS = Constants.COMBAT_BRACELET_IDS;
    private static final int BASE_COMBAT_BRACELET_ID = Constants.BASE_COMBAT_BRACELET_ID;

    public boolean isCombatBracelet(int itemId) {
        return Constants.isCombatBracelet(itemId);
    }

    public List<Integer> getHerbPatchIds() {
        return Constants.HERB_PATCH_IDS;
    }

    public List<Integer> getHopsPatchIds() {
        return Constants.HOPS_PATCH_IDS;
    }

    public List<Integer> getBirdhousePatchIds() {
        return Constants.BIRDHOUSE_PATCH_IDS;
    }

    public List<Integer> getBirdhousePatchIdsForLocation(String locationName) {
        return Constants.BIRDHOUSE_PATCH_IDS_BY_LOCATION.getOrDefault(locationName, Collections.emptyList());
    }

    private static final List<Integer> HERB_SEED_IDS = Arrays.asList(
            ItemID.GUAM_SEED, ItemID.MARRENTILL_SEED, ItemID.TARROMIN_SEED, ItemID.HARRALANDER_SEED,
            ItemID.RANARR_SEED, ItemID.TOADFLAX_SEED, ItemID.IRIT_SEED, ItemID.AVANTOE_SEED,
            ItemID.KWUARM_SEED, ItemID.SNAPDRAGON_SEED, ItemID.CADANTINE_SEED, ItemID.LANTADYME_SEED,
            ItemID.DWARF_WEED_SEED, ItemID.TORSTOL_SEED, ItemID.HUASCA_SEED);
    private static final int BASE_SEED_ID = ItemID.GUAM_SEED;
    private static final int BASE_HOPS_SEED_ID = ItemID.BARLEY_SEED;

    public List<Integer> getHerbSeedIds() {
        return HERB_SEED_IDS;
    }

    public List<Integer> getHopsSeedIds() {
        return Constants.HOPS_SEED_IDS;
    }

    public List<Integer> getFlowerSeedIds() {
        return Constants.FLOWER_SEED_IDS;
    }

    private boolean isHerbSeed(int itemId) {
        return HERB_SEED_IDS.contains(itemId);
    }

    private boolean isHopsSeed(int itemId) {
        return Constants.HOPS_SEED_IDS.contains(itemId);
    }

    private boolean isFlowerSeed(int itemId) {
        return Constants.isFlowerSeed(itemId);
    }

    public List<Integer> getFlowerPatchIds() {
        return Constants.FLOWER_PATCH_IDS;
    }

    @Deprecated
    public List<Integer> getAllotmentPatchIds() {
        return Constants.ALLOTMENT_PATCH_IDS_BY_LOCATION.getOrDefault("Ardougne", Collections.emptyList());
    }

    public List<Integer> getAllotmentPatchIdsForLocation(String locationName) {
        return Constants.ALLOTMENT_PATCH_IDS_BY_LOCATION.getOrDefault(locationName, Collections.emptyList());
    }

    public Integer getHerbPatchIdForLocation(String locationName) {
        return Constants.HERB_PATCH_IDS_BY_LOCATION.get(locationName);
    }

    public Integer getFlowerPatchIdForLocation(String locationName) {
        return Constants.FLOWER_PATCH_IDS_BY_LOCATION.get(locationName);
    }

    public Integer getHopsPatchIdForLocation(String locationName) {
        return Constants.HOPS_PATCH_IDS_BY_LOCATION.get(locationName);
    }

    public Integer getFruitTreePatchIdForLocation(String locationName) {
        return Constants.FRUIT_TREE_PATCH_IDS_BY_LOCATION.get(locationName);
    }

    private static final List<Integer> ALLOTMENT_SEED_IDS = Arrays.asList(
            ItemID.POTATO_SEED, ItemID.ONION_SEED, ItemID.CABBAGE_SEED, ItemID.TOMATO_SEED,
            ItemID.SWEETCORN_SEED, ItemID.STRAWBERRY_SEED, ItemID.WATERMELON_SEED, ItemID.SNAPE_GRASS_SEED);
    private static final int BASE_ALLOTMENT_SEED_ID = ItemID.SNAPE_GRASS_SEED;

    public List<Integer> getAllotmentSeedIds() {
        return Constants.ALLOTMENT_SEED_IDS;
    }

    private boolean isAllotmentSeed(int itemId) {
        return Constants.isAllotmentSeed(itemId);
    }

    public List<Integer> getTreePatchIds() {
        return Constants.TREE_PATCH_IDS;
    }

    private static final List<Integer> TREE_SAPLING_IDS = Arrays.asList(
            ItemID.PLANTPOT_OAK_SAPLING, ItemID.PLANTPOT_WILLOW_SAPLING,
            ItemID.PLANTPOT_MAPLE_SAPLING, ItemID.PLANTPOT_YEW_SAPLING,
            ItemID.PLANTPOT_MAGIC_TREE_SAPLING
    );

    private static final int BASE_SAPLING_ID = ItemID.PLANTPOT_OAK_SAPLING;

    public List<Integer> getTreeSaplingIds() {
        return TREE_SAPLING_IDS;
    }

    private boolean isTreeSapling(int itemId) {
        return TREE_SAPLING_IDS.contains(itemId);
    }

    private static final List<Integer> HARDWOOD_SAPLING_IDS = Arrays.asList(
            ItemID.PLANTPOT_TEAK_SAPLING,
            ItemID.PLANTPOT_MAHOGANY_SAPLING
    );

    public static final int BASE_HARDWOOD_SAPLING_ID = ItemID.PLANTPOT_TEAK_SAPLING;

    public List<Integer> getHardwoodSaplingIds() {
        return HARDWOOD_SAPLING_IDS;
    }

    private boolean isHardwoodSapling(int itemId) {
        return HARDWOOD_SAPLING_IDS.contains(itemId);
    }

    public List<Integer> getFruitTreePatchIds() {
        return Constants.FRUIT_TREE_PATCH_IDS;
    }

    private static final List<Integer> FRUIT_TREE_SAPLING_IDS = Arrays.asList(ItemID.PLANTPOT_APPLE_SAPLING,
            ItemID.PLANTPOT_BANANA_SAPLING, ItemID.PLANTPOT_ORANGE_SAPLING, ItemID.PLANTPOT_CURRY_SAPLING,
            ItemID.PLANTPOT_PINEAPPLE_SAPLING, ItemID.PLANTPOT_PAPAYA_SAPLING, ItemID.PLANTPOT_PALM_SAPLING,
            ItemID.PLANTPOT_DRAGONFRUIT_SAPLING);
    private static final int BASE_FRUIT_SAPLING_ID = ItemID.PLANTPOT_APPLE_SAPLING;

    public List<Integer> getFruitTreeSaplingIds() {
        return FRUIT_TREE_SAPLING_IDS;
    }

    private boolean isFruitTreeSapling(int itemId) {
        return FRUIT_TREE_SAPLING_IDS.contains(itemId);
    }

    public static final List<Integer> RUNE_POUCH_ID = Arrays.asList(ItemID.BH_RUNE_POUCH, ItemID.DIVINE_RUNE_POUCH);

    public static final List<Integer> RUNE_POUCH_AMOUNT_VARBITS = Arrays.asList(VarbitID.RUNE_POUCH_QUANTITY_1,
            VarbitID.RUNE_POUCH_QUANTITY_2, VarbitID.RUNE_POUCH_QUANTITY_3, VarbitID.RUNE_POUCH_QUANTITY_4);

    public static final List<Integer> RUNE_POUCH_RUNE_VARBITS = Arrays.asList(VarbitID.RUNE_POUCH_TYPE_1,
            VarbitID.RUNE_POUCH_TYPE_2, VarbitID.RUNE_POUCH_TYPE_3, VarbitID.RUNE_POUCH_TYPE_4);

    private static final Map<Integer, List<Integer>> COMBINATION_RUNE_SUBRUNES_MAP;
    private static final Map<Integer, List<Integer>> STAFF_RUNES_MAP;
    private static final int STAFF_RUNE_AMOUNT = 999;

    static {
        Map<Integer, List<Integer>> tempMap = new HashMap<>();
        tempMap.put(ItemID.DUSTRUNE, Arrays.asList(ItemID.AIRRUNE, ItemID.EARTHRUNE));
        tempMap.put(ItemID.MISTRUNE, Arrays.asList(ItemID.AIRRUNE, ItemID.WATERRUNE));
        tempMap.put(ItemID.MUDRUNE, Arrays.asList(ItemID.WATERRUNE, ItemID.EARTHRUNE));
        tempMap.put(ItemID.LAVARUNE, Arrays.asList(ItemID.FIRERUNE, ItemID.EARTHRUNE));
        tempMap.put(ItemID.STEAMRUNE, Arrays.asList(ItemID.FIRERUNE, ItemID.WATERRUNE));
        tempMap.put(ItemID.SMOKERUNE, Arrays.asList(ItemID.FIRERUNE, ItemID.AIRRUNE));
        COMBINATION_RUNE_SUBRUNES_MAP = Collections.unmodifiableMap(tempMap);

        Map<Integer, List<Integer>> staffMap = new HashMap<>();
        staffMap.put(ItemID.STAFF_OF_AIR, Arrays.asList(ItemID.AIRRUNE));
        staffMap.put(ItemID.AIR_BATTLESTAFF, Arrays.asList(ItemID.AIRRUNE));
        staffMap.put(ItemID.MYSTIC_AIR_STAFF, Arrays.asList(ItemID.AIRRUNE));
        staffMap.put(ItemID.STAFF_OF_WATER, Arrays.asList(ItemID.WATERRUNE));
        staffMap.put(ItemID.WATER_BATTLESTAFF, Arrays.asList(ItemID.WATERRUNE));
        staffMap.put(ItemID.MYSTIC_WATER_STAFF, Arrays.asList(ItemID.WATERRUNE));
        staffMap.put(ItemID.STAFF_OF_EARTH, Arrays.asList(ItemID.EARTHRUNE));
        staffMap.put(ItemID.EARTH_BATTLESTAFF, Arrays.asList(ItemID.EARTHRUNE));
        staffMap.put(ItemID.MYSTIC_EARTH_STAFF, Arrays.asList(ItemID.EARTHRUNE));
        staffMap.put(ItemID.STAFF_OF_FIRE, Arrays.asList(ItemID.FIRERUNE));
        staffMap.put(ItemID.FIRE_BATTLESTAFF, Arrays.asList(ItemID.FIRERUNE));
        staffMap.put(ItemID.MYSTIC_FIRE_STAFF, Arrays.asList(ItemID.FIRERUNE));
        staffMap.put(ItemID.LAVA_BATTLESTAFF, Arrays.asList(ItemID.FIRERUNE, ItemID.EARTHRUNE));
        staffMap.put(ItemID.MYSTIC_LAVA_STAFF, Arrays.asList(ItemID.FIRERUNE, ItemID.EARTHRUNE));
        staffMap.put(ItemID.STEAM_BATTLESTAFF, Arrays.asList(ItemID.FIRERUNE, ItemID.WATERRUNE));
        staffMap.put(ItemID.MYSTIC_STEAM_BATTLESTAFF, Arrays.asList(ItemID.FIRERUNE, ItemID.WATERRUNE));
        staffMap.put(ItemID.MIST_BATTLESTAFF, Arrays.asList(ItemID.AIRRUNE, ItemID.WATERRUNE));
        staffMap.put(ItemID.MYSTIC_MIST_BATTLESTAFF, Arrays.asList(ItemID.AIRRUNE, ItemID.WATERRUNE));
        staffMap.put(ItemID.DUST_BATTLESTAFF, Arrays.asList(ItemID.AIRRUNE, ItemID.EARTHRUNE));
        staffMap.put(ItemID.MYSTIC_DUST_BATTLESTAFF, Arrays.asList(ItemID.AIRRUNE, ItemID.EARTHRUNE));
        staffMap.put(ItemID.SMOKE_BATTLESTAFF, Arrays.asList(ItemID.FIRERUNE, ItemID.AIRRUNE));
        staffMap.put(ItemID.MYSTIC_SMOKE_BATTLESTAFF, Arrays.asList(ItemID.FIRERUNE, ItemID.AIRRUNE));
        staffMap.put(ItemID.MUD_BATTLESTAFF, Arrays.asList(ItemID.WATERRUNE, ItemID.EARTHRUNE));
        staffMap.put(ItemID.MYSTIC_MUD_STAFF, Arrays.asList(ItemID.WATERRUNE, ItemID.EARTHRUNE));
        staffMap.put(30634, Arrays.asList(ItemID.FIRERUNE, ItemID.WATERRUNE));
        staffMap.put(30064, Arrays.asList(ItemID.EARTHRUNE));
        STAFF_RUNES_MAP = Collections.unmodifiableMap(staffMap);
    }

    private int getRuneItemIdFromVarbitValue(int varbitValue) {
        switch (varbitValue) {
            case 1: return ItemID.AIRRUNE;
            case 2: return ItemID.WATERRUNE;
            case 3: return ItemID.EARTHRUNE;
            case 4: return ItemID.FIRERUNE;
            case 5: return ItemID.MINDRUNE;
            case 6: return ItemID.CHAOSRUNE;
            case 7: return ItemID.DEATHRUNE;
            case 8: return ItemID.BLOODRUNE;
            case 9: return ItemID.COSMICRUNE;
            case 10: return ItemID.NATURERUNE;
            case 11: return ItemID.LAWRUNE;
            case 12: return ItemID.BODYRUNE;
            case 13: return ItemID.SOULRUNE;
            case 14: return ItemID.ASTRALRUNE;
            case 15: return ItemID.MISTRUNE;
            case 16: return ItemID.MUDRUNE;
            case 17: return ItemID.DUSTRUNE;
            case 18: return ItemID.LAVARUNE;
            case 19: return ItemID.STEAMRUNE;
            case 20: return ItemID.SMOKERUNE;
            case 21: return ItemID.WRATHRUNE;
            default: return -1;
        }
    }

    private Map<Integer, Integer> getRunePouchContentsVarbits() {
        Map<Integer, Integer> runePouchContents = new HashMap<>();
        for (int i = 0; i < RUNE_POUCH_RUNE_VARBITS.size(); i++) {
            int runeVarbitValue = client.getVarbitValue(RUNE_POUCH_RUNE_VARBITS.get(i));
            int runeAmount = client.getVarbitValue(RUNE_POUCH_AMOUNT_VARBITS.get(i));
            int runeId = getRuneItemIdFromVarbitValue(runeVarbitValue);
            if (runeId != -1 && runeAmount > 0) {
                handleCombinationRunes(runeId, runeAmount, runePouchContents);
            }
        }
        return runePouchContents;
    }

    private Map<Integer, Integer> buildExpandedRuneMap(Item[] items) {
        Map<Integer, Integer> expandedRuneMap = new HashMap<>(getRunePouchContentsVarbits());
        for (Item item : items) {
            if (item != null) {
                int itemIdRune = item.getId();
                int itemQuantity = item.getQuantity();

                if (COMBINATION_RUNE_SUBRUNES_MAP.containsKey(itemIdRune)) {
                    List<Integer> subRunes = COMBINATION_RUNE_SUBRUNES_MAP.get(itemIdRune);
                    for (int subRune : subRunes) {
                        expandedRuneMap.put(subRune, expandedRuneMap.getOrDefault(subRune, 0) + itemQuantity);
                    }
                } else if (STAFF_RUNES_MAP.containsKey(itemIdRune)) {
                    List<Integer> staffRunes = STAFF_RUNES_MAP.get(itemIdRune);
                    for (int rune : staffRunes) {
                        expandedRuneMap.put(rune, Math.max(expandedRuneMap.getOrDefault(rune, 0), STAFF_RUNE_AMOUNT));
                    }
                } else {
                    String itemName = itemManager.getItemComposition(itemIdRune).getName().toLowerCase();
                    if (itemName.contains("rune")) {
                        expandedRuneMap.put(itemIdRune, expandedRuneMap.getOrDefault(itemIdRune, 0) + itemQuantity);
                    }
                }
            }
        }

        Map<Integer, Integer> equippedItems = getEquippedItems();
        for (Map.Entry<Integer, Integer> equippedEntry : equippedItems.entrySet()) {
            int equippedItemId = equippedEntry.getKey();
            if (STAFF_RUNES_MAP.containsKey(equippedItemId)) {
                List<Integer> staffRunes = STAFF_RUNES_MAP.get(equippedItemId);
                for (int rune : staffRunes) {
                    expandedRuneMap.put(rune, Math.max(expandedRuneMap.getOrDefault(rune, 0), STAFF_RUNE_AMOUNT));
                }
            }
        }
        return expandedRuneMap;
    }

    @Inject
    public EasyFarmingOverlay(Client client, EasyFarmingPlugin plugin, ItemManager itemManager,
                              InfoBoxManager infoBoxManager) {
        this.client = client;
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.infoBoxManager = infoBoxManager;
        setPosition(OverlayPosition.BOTTOM_RIGHT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    private void handleCombinationRunes(int runeId, int runeAmount, Map<Integer, Integer> runePouchContents) {
        if (COMBINATION_RUNE_SUBRUNES_MAP.containsKey(runeId)) {
            List<Integer> subRunes = COMBINATION_RUNE_SUBRUNES_MAP.get(runeId);
            for (int subRune : subRunes) {
                runePouchContents.put(subRune, runePouchContents.getOrDefault(subRune, 0) + runeAmount);
            }
        } else {
            runePouchContents.put(runeId, runeAmount);
        }
    }

    public Integer checkToolLep(Integer item) {
        if (item == ItemID.BUCKET_COMPOST) return client.getVarbitValue(1442);
        if (item == ItemID.BUCKET_SUPERCOMPOST) return client.getVarbitValue(1443);
        if (item == ItemID.BUCKET_ULTRACOMPOST) return client.getVarbitValue(5732);
        if (item == ItemID.BOTTOMLESS_COMPOST_BUCKET) {
            if (client.getVarbitValue(7915) != 0) return 1;
        }
        return 0;
    }

    private int getSkillsNecklaceCharges(int itemId) {
        switch (itemId) {
            case ItemID.JEWL_NECKLACE_OF_SKILLS_1: return 1;
            case ItemID.JEWL_NECKLACE_OF_SKILLS_2: return 2;
            case ItemID.JEWL_NECKLACE_OF_SKILLS_3: return 3;
            case ItemID.JEWL_NECKLACE_OF_SKILLS_4: return 4;
            case ItemID.JEWL_NECKLACE_OF_SKILLS_5: return 5;
            case ItemID.JEWL_NECKLACE_OF_SKILLS_6: return 6;
            default: return 0;
        }
    }

    public boolean isQuetzalWhistle(int itemId) {
        return itemId == ItemID.HG_QUETZALWHISTLE_BASIC ||
                itemId == ItemID.HG_QUETZALWHISTLE_ENHANCED ||
                itemId == ItemID.HG_QUETZALWHISTLE_PERFECTED;
    }

    public boolean isRoyalSeedPod(int itemId) {
        return itemId == ItemID.MM2_ROYAL_SEED_POD;
    }

    public boolean isEctophial(int itemId) {
        return itemId == ItemID.ECTOPHIAL;
    }

    private Map<Integer, Integer> getEquippedItems() {
        Map<Integer, Integer> equippedItems = new HashMap<>();
        ItemContainer equipment = client.getItemContainer(94);
        if (equipment == null) return equippedItems;

        Item[] items = equipment.getItems();
        if (items == null) return equippedItems;

        for (EquipmentInventorySlot slot : EquipmentInventorySlot.values()) {
            try {
                int slotIdx = slot.getSlotIdx();
                if (slotIdx >= 0 && slotIdx < items.length) {
                    Item item = items[slotIdx];
                    if (item != null && item.getId() > 0) {
                        int itemId = item.getId();
                        equippedItems.put(itemId, equippedItems.getOrDefault(itemId, 0) + 1);
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
        return equippedItems;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.isOverlayActive()) {
            clearAllInfoBoxes();
            return null;
        }

        if (!plugin.areItemsCollected()) {
            plugin.addTextToInfoBox("Grab all the items needed:");
            Map<Integer, Integer> itemsToCheck = null;
            if (plugin.getFarmingTeleportOverlay().isCustomRunMode()
                    && !plugin.getFarmingTeleportOverlay().getCustomRunLocations().isEmpty()) {
                itemsToCheck = CustomRunItemRequirements.buildRequirements(
                        plugin.getLocationCatalog(),
                        plugin.getConfig(),
                        client,
                        plugin.getFarmingTeleportOverlay().getCustomRunLocations(),
                        plugin.getCustomRunIncludeSecateurs(),
                        plugin.getCustomRunIncludeDibber(),
                        plugin.getCustomRunIncludeRake());
            }
            if (itemsToCheck == null) return null;

            ItemContainer inventory = client.getItemContainer(InventoryID.INV);
            Item[] items;
            if (inventory == null || inventory.getItems() == null) {
                items = new Item[0];
            } else {
                items = inventory.getItems();
            }

            Map<Integer, Integer> expandedRuneMap = buildExpandedRuneMap(items);

            int teleportCrystalCount = 0;
            for (Item item : items) {
                if (isTeleportCrystal(item.getId())) teleportCrystalCount += item.getQuantity();
            }

            int digsitePendantCount = 0;
            for (Item item : items) {
                if (Constants.DIGSITE_PENDANT_IDS.contains(item.getId())) digsitePendantCount += 1;
            }

            int skillsNecklaceCharges = 0;
            for (Item item : items) {
                if (isSkillsNecklace(item.getId())) {
                    skillsNecklaceCharges += getSkillsNecklaceCharges(item.getId()) * item.getQuantity();
                }
            }
            Map<Integer, Integer> equippedItems = getEquippedItems();
            for (Map.Entry<Integer, Integer> equippedEntry : equippedItems.entrySet()) {
                if (isSkillsNecklace(equippedEntry.getKey())) {
                    skillsNecklaceCharges += getSkillsNecklaceCharges(equippedEntry.getKey());
                }
            }

            int quetzalWhistleCount = 0;
            for (Item item : items) {
                if (isQuetzalWhistle(item.getId())) quetzalWhistleCount += item.getQuantity();
            }

            int combatBraceletCharges = 0;
            for (Item item : items) {
                if (isCombatBracelet(item.getId())) {
                    combatBraceletCharges += Constants.getCombatBraceletCharges(item.getId()) * item.getQuantity();
                }
            }
            for (Map.Entry<Integer, Integer> equippedEntry : equippedItems.entrySet()) {
                if (isCombatBracelet(equippedEntry.getKey())) {
                    combatBraceletCharges += Constants.getCombatBraceletCharges(equippedEntry.getKey());
                }
            }

            int totalSeeds = 0;
            int totalAllotmentSeeds = 0;
            int totalTreeSaplings = 0;
            int totalHardwoodSaplings = 0;
            int totalFruitTreeSaplings = 0;
            int totalHopsSeeds = 0;
            int totalFlowerSeeds = 0;
            boolean customRun = plugin.getFarmingTeleportOverlay().isCustomRunMode();

            if (customRun) {
                for (Item item : items) {
                    if (isHerbSeed(item.getId())) totalSeeds += item.getQuantity();
                    if (isAllotmentSeed(item.getId())) totalAllotmentSeeds += item.getQuantity();
                    if (isFlowerSeed(item.getId())) totalFlowerSeeds += item.getQuantity();
                    if (isTreeSapling(item.getId())) totalTreeSaplings += item.getQuantity();
                    if (isHardwoodSapling(item.getId())) totalHardwoodSaplings += item.getQuantity();
                    if (isFruitTreeSapling(item.getId())) totalFruitTreeSaplings += item.getQuantity();
                    if (isHopsSeed(item.getId())) totalHopsSeeds += item.getQuantity();
                }
            }

            panelComponent.getChildren().clear();
            Map<Integer, Integer> inventoryItemCounts = new HashMap<>();
            boolean hasRunePouch = false;

            for (Item item : items) {
                if (item != null) {
                    int itemId = item.getId();
                    int itemQuantity = item.getQuantity();
                    if (RUNE_POUCH_ID.contains(itemId)) hasRunePouch = true;

                    if (COMBINATION_RUNE_SUBRUNES_MAP.containsKey(itemId)) {
                        List<Integer> subRunes = COMBINATION_RUNE_SUBRUNES_MAP.get(itemId);
                        for (int subRune : subRunes) {
                            inventoryItemCounts.put(subRune, inventoryItemCounts.getOrDefault(subRune, 0) + itemQuantity);
                        }
                    } else if (STAFF_RUNES_MAP.containsKey(itemId)) {
                        List<Integer> staffRunes = STAFF_RUNES_MAP.get(itemId);
                        for (int rune : staffRunes) {
                            inventoryItemCounts.put(rune, Math.max(inventoryItemCounts.getOrDefault(rune, 0), STAFF_RUNE_AMOUNT));
                        }
                    } else {
                        inventoryItemCounts.put(itemId, inventoryItemCounts.getOrDefault(itemId, 0) + itemQuantity);
                    }
                }
            }

            if (hasRunePouch) {
                for (Map.Entry<Integer, Integer> runeEntry : expandedRuneMap.entrySet()) {
                    int runeId = runeEntry.getKey();
                    int runeCount = runeEntry.getValue();
                    inventoryItemCounts.put(runeId, inventoryItemCounts.getOrDefault(runeId, 0) + runeCount);
                }
            }

            for (Map.Entry<Integer, Integer> equippedEntry : equippedItems.entrySet()) {
                int equippedItemId = equippedEntry.getKey();
                int equippedCount = equippedEntry.getValue();

                if (STAFF_RUNES_MAP.containsKey(equippedItemId)) {
                    List<Integer> staffRunes = STAFF_RUNES_MAP.get(equippedItemId);
                    for (int rune : staffRunes) {
                        inventoryItemCounts.put(rune, Math.max(inventoryItemCounts.getOrDefault(rune, 0), STAFF_RUNE_AMOUNT));
                    }
                } else {
                    inventoryItemCounts.put(equippedItemId, inventoryItemCounts.getOrDefault(equippedItemId, 0) + equippedCount);
                }
            }

            List<AbstractMap.SimpleEntry<Integer, Integer>> missingItemsWithCounts = new ArrayList<>();
            List<String> textForOverlay = new ArrayList<>(); // NEW: The list that gets sent to the UI

            for (Map.Entry<Integer, Integer> entry : itemsToCheck.entrySet()) {
                int itemId = entry.getKey();
                int count = entry.getValue();

                int inventoryCount = inventoryItemCounts.getOrDefault(itemId, 0);

                if (itemId == ItemID.DRAMEN_STAFF) {
                    inventoryCount += inventoryItemCounts.getOrDefault(LUNAR_STAFF_ITEM_ID, 0);
                }

                if (itemId == ItemID.BOTTOMLESS_COMPOST_BUCKET) {
                    for (Item item : items) {
                        if (item != null && BOTTOMLESS_COMPOST_BUCKET_IDS.contains(item.getId())) {
                            inventoryCount = 1;
                            break;
                        }
                    }
                }

                int toolLepCount = checkToolLep(itemId);
                if (toolLepCount > 0) inventoryCount += toolLepCount;

                if (customRun && itemId == BASE_SEED_ID) inventoryCount = totalSeeds;
                else if (customRun && itemId == ItemID.LIMPWURT_SEED) inventoryCount = totalFlowerSeeds;
                else if (customRun && itemId == BASE_ALLOTMENT_SEED_ID) inventoryCount = totalAllotmentSeeds;
                else if (customRun && itemId == BASE_SAPLING_ID) inventoryCount = totalTreeSaplings;
                else if (customRun && itemId == BASE_HARDWOOD_SAPLING_ID) inventoryCount = totalHardwoodSaplings;
                else if (customRun && itemId == BASE_FRUIT_SAPLING_ID) inventoryCount = totalFruitTreeSaplings;
                else if (customRun && itemId == BASE_HOPS_SEED_ID) inventoryCount = totalHopsSeeds;
                else if (itemId == ItemID.LOGS) {
                    int totalLogs = 0;
                    for (int logId : BIRD_HOUSE_LOG_IDS) totalLogs += inventoryItemCounts.getOrDefault(logId, 0);
                    inventoryCount = totalLogs;
                } else if (itemId == ItemID.HAMMERSTONE_HOP_SEED) {
                    int totalBirdSeeds = 0;
                    for (int seedId : BIRD_HOUSE_SEED_IDS) totalBirdSeeds += inventoryItemCounts.getOrDefault(seedId, 0);
                    inventoryCount = totalBirdSeeds;
                } else if (itemId == Constants.BASE_DIGSITE_PENDANT_ID) {
                    inventoryCount = digsitePendantCount;
                } else if (itemId == BASE_TELEPORT_CRYSTAL_ID) {
                    inventoryCount = teleportCrystalCount;
                } else if (itemId == BASE_SKILLS_NECKLACE_ID) {
                    inventoryCount = skillsNecklaceCharges;
                } else if (itemId == ItemID.HG_QUETZALWHISTLE_BASIC) {
                    inventoryCount = quetzalWhistleCount;
                } else if (itemId == BASE_EXPLORERS_RING_ID) {
                    boolean hasExplorersRing = false;
                    for (int ringId : EXPLORERS_RING_IDS) {
                        if ((inventoryItemCounts.containsKey(ringId) && inventoryItemCounts.get(ringId) > 0) ||
                                (equippedItems.containsKey(ringId) && equippedItems.get(ringId) > 0)) {
                            hasExplorersRing = true;
                            break;
                        }
                    }
                    inventoryCount = hasExplorersRing ? 1 : 0;
                } else if (itemId == BASE_ARDY_CLOAK_ID) {
                    boolean hasArdyCloak = false;
                    for (int cloakId : ARDY_CLOAK_IDS) {
                        if (inventoryItemCounts.containsKey(cloakId) && inventoryItemCounts.get(cloakId) > 0) {
                            hasArdyCloak = true;
                            break;
                        }
                    }
                    inventoryCount = hasArdyCloak ? 1 : 0;
                } else if (itemId == BASE_WATERING_CAN_ID) {
                    boolean hasWateringCan = false;
                    for (int canId : WATERING_CAN_IDS) {
                        if (inventoryItemCounts.containsKey(canId) && inventoryItemCounts.get(canId) > 0) {
                            hasWateringCan = true;
                            break;
                        }
                    }
                    inventoryCount = hasWateringCan ? 1 : 0;
                } else if (itemId == BASE_COMBAT_BRACELET_ID) {
                    inventoryCount = combatBraceletCharges;
                }

                if (inventoryCount < count) {
                    int missingCount = count - inventoryCount;
                    BufferedImage itemImage = itemManager.getImage(itemId);
                    if (itemImage != null) {
                        missingItemsWithCounts.add(new AbstractMap.SimpleEntry<>(itemId, missingCount));

                        // NEW: Formatting the name nicely for the Text Box list
                        String baseName = itemManager.getItemComposition(itemId).getName();
                        if (isTreeSapling(itemId)) baseName = "Tree sapling";
                        else if (isFruitTreeSapling(itemId)) baseName = "Fruit tree sapling";
                        else if (isHardwoodSapling(itemId)) baseName = "Hardwood sapling";
                        else if (isHerbSeed(itemId)) baseName = "Herb seed";
                        else if (isFlowerSeed(itemId)) baseName = "Flower seed";
                        else if (isAllotmentSeed(itemId)) baseName = "Allotment seed";
                        else if (isHopsSeed(itemId)) baseName = "Hops seed";

                        textForOverlay.add(missingCount + "x " + baseName);
                    }
                }
            }

            missingItemsWithCounts.sort((entry1, entry2) -> {
                int priority1 = getItemPriority(entry1.getKey());
                int priority2 = getItemPriority(entry2.getKey());
                return Integer.compare(priority1, priority2);
            });

            // NEW: Send the updated text list to the UI
            if (plugin.getEasyFarmingOverlayInfoBox() != null) {
                if (!textForOverlay.isEmpty()) {
                    plugin.getEasyFarmingOverlayInfoBox().setMissingItemsList(textForOverlay);
                } else {
                    plugin.getEasyFarmingOverlayInfoBox().setMissingItemsList(null);
                }
            }

            plugin.setTeleportOverlayActive(missingItemsWithCounts.isEmpty());

            Set<Integer> currentMissingItemIds = new HashSet<>();
            for (AbstractMap.SimpleEntry<Integer, Integer> pair : missingItemsWithCounts) {
                currentMissingItemIds.add(pair.getKey());
            }

            if (infoBoxManager != null) {
                Iterator<Map.Entry<Integer, RequiredItemInfoBox>> iterator = currentInfoBoxes.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, RequiredItemInfoBox> entry = iterator.next();
                    int itemId = entry.getKey();
                    if (!currentMissingItemIds.contains(itemId)) {
                        infoBoxManager.removeInfoBox(entry.getValue());
                        iterator.remove();
                    }
                }
            }

            if (infoBoxManager != null) {
                for (AbstractMap.SimpleEntry<Integer, Integer> pair : missingItemsWithCounts) {
                    int itemId = pair.getKey();
                    int missingCount = pair.getValue();

                    BufferedImage itemImage = itemManager.getImage(itemId);
                    if (itemImage != null) {
                        String itemName = itemManager.getItemComposition(itemId).getName();
                        if (isTreeSapling(itemId)) itemName = missingCount == 1 ? "tree sapling" : "tree saplings";
                        else if (isFruitTreeSapling(itemId)) itemName = missingCount == 1 ? "fruit tree sapling" : "fruit tree saplings";
                        else if (isHardwoodSapling(itemId)) itemName = missingCount == 1 ? "hardwood sapling" : "hardwood saplings";
                        else if (isHerbSeed(itemId)) itemName = missingCount == 1 ? "herb seed" : "herb seeds";
                        else if (isFlowerSeed(itemId)) itemName = missingCount == 1 ? "flower seed" : "flower seeds";
                        else if (isAllotmentSeed(itemId)) itemName = missingCount == 1 ? "allotment seed" : "allotment seeds";
                        else if (isHopsSeed(itemId)) itemName = missingCount == 1 ? "hops seed" : "hops seeds";

                        RequiredItemInfoBox existingInfoBox = currentInfoBoxes.get(itemId);
                        if (existingInfoBox != null) {
                            if (existingInfoBox.getMissingCount() != missingCount) {
                                infoBoxManager.removeInfoBox(existingInfoBox);
                                RequiredItemInfoBox newInfoBox = new RequiredItemInfoBox(itemImage, plugin, itemId, missingCount, itemName);
                                infoBoxManager.addInfoBox(newInfoBox);
                                currentInfoBoxes.put(itemId, newInfoBox);
                            }
                        } else {
                            RequiredItemInfoBox infoBox = new RequiredItemInfoBox(itemImage, plugin, itemId, missingCount, itemName);
                            infoBoxManager.addInfoBox(infoBox);
                            currentInfoBoxes.put(itemId, infoBox);
                        }
                    }
                }
            }

            if (missingItemsWithCounts.isEmpty()) {
                plugin.setItemsCollected(true);
            } else {
                plugin.setItemsCollected(false);
            }

            return panelComponent.render(graphics);
        }

        clearAllInfoBoxes();
        // Clear the list from the UI when everything is collected
        if (plugin.getEasyFarmingOverlayInfoBox() != null) {
            plugin.getEasyFarmingOverlayInfoBox().setMissingItemsList(null);
        }
        return null;
    }

    private void clearAllInfoBoxes() {
        if (infoBoxManager != null) {
            for (RequiredItemInfoBox infoBox : currentInfoBoxes.values()) {
                infoBoxManager.removeInfoBox(infoBox);
            }
        }
        currentInfoBoxes.clear();
    }

    private int getItemPriority(int itemId) {
        if (isTool(itemId)) return 0;
        if (isConsumableSupply(itemId)) return 3;
        if (isBasalt(itemId)) return 2;
        return 1;
    }

    private boolean isTool(int itemId) {
        return itemId == ItemID.SPADE ||
                itemId == ItemID.RAKE ||
                itemId == ItemID.DIBBER ||
                itemId == ItemID.FAIRY_ENCHANTED_SECATEURS ||
                isWateringCan(itemId);
    }

    private boolean isBasalt(int itemId) {
        return itemId == ItemID.STRONGHOLD_TELEPORT_BASALT ||
                itemId == ItemID.WEISS_TELEPORT_BASALT;
    }

    private boolean isConsumableSupply(int itemId) {
        if (isHerbSeed(itemId) || isTreeSapling(itemId) || isHardwoodSapling(itemId) ||
                isFruitTreeSapling(itemId) || isHopsSeed(itemId) || isAllotmentSeed(itemId) ||
                isFlowerSeed(itemId)) return true;

        if (itemId == ItemID.BUCKET_COMPOST || itemId == ItemID.BUCKET_SUPERCOMPOST ||
                itemId == ItemID.BUCKET_ULTRACOMPOST || itemId == ItemID.BOTTOMLESS_COMPOST_BUCKET ||
                BOTTOMLESS_COMPOST_BUCKET_IDS.contains(itemId)) return true;

        if (itemId == BASE_SEED_ID || itemId == BASE_SAPLING_ID || itemId == BASE_FRUIT_SAPLING_ID ||
                itemId == BASE_HOPS_SEED_ID || itemId == BASE_ALLOTMENT_SEED_ID) return true;

        return false;
    }
}