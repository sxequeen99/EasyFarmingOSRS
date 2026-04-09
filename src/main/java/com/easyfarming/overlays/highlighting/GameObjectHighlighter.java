package com.easyfarming.overlays.highlighting;

import com.easyfarming.EasyFarmingPlugin;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import com.easyfarming.utils.Constants;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles highlighting of game objects in the world.
 * Upgraded to scan ALL z-planes to catch objects hidden on weird elevations (like Fossil Island hills).
 */
public class GameObjectHighlighter {
    private final Client client;
    private final EasyFarmingPlugin plugin;

    private final Map<Integer, List<GameObject>> objectCache = new HashMap<>();
    private final Map<Integer, List<DecorativeObject>> decorativeObjectCache = new HashMap<>();
    private final Map<Integer, List<GroundObject>> groundObjectCache = new HashMap<>();
    private int lastSceneKey = -1;
    private long lastCacheClearTime = 0;

    @Inject
    public GameObjectHighlighter(Client client, EasyFarmingPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
    }

    private void checkAndClearCache(int sceneKey) {
        long now = System.currentTimeMillis();
        if (sceneKey != lastSceneKey || (now - lastCacheClearTime > 2000)) {
            objectCache.clear();
            decorativeObjectCache.clear();
            groundObjectCache.clear();
            lastSceneKey = sceneKey;
            lastCacheClearTime = now;
        }
    }

    public List<GameObject> findGameObjectsByID(int objectID) {
        if (client.getLocalPlayer() == null) return new ArrayList<>();
        WorldView wv = client.getTopLevelWorldView();
        if (wv == null || wv.getScene() == null) return new ArrayList<>();

        int regionId = client.getLocalPlayer().getWorldLocation().getRegionID();
        checkAndClearCache(regionId * 10 + wv.getPlane());

        List<GameObject> cached = objectCache.get(objectID);
        if (cached != null) return cached;

        List<GameObject> gameObjects = new ArrayList<>();
        Tile[][][] tiles = wv.getScene().getTiles();

        // FIX: Scan ALL planes (z) so we don't miss objects anchored below the hill!
        for (int z = 0; z < tiles.length; z++) {
            for (int x = 0; x < Constants.SCENE_SIZE; x++) {
                for (int y = 0; y < Constants.SCENE_SIZE; y++) {
                    Tile tile = tiles[z][x][y];
                    if (tile == null) continue;
                    GameObject[] objectsArray = tile.getGameObjects();
                    if (objectsArray != null) {
                        for (GameObject gameObject : objectsArray) {
                            if (gameObject != null && objectIdMatches(gameObject.getId(), objectID)) {
                                gameObjects.add(gameObject);
                            }
                        }
                    }
                }
            }
        }
        objectCache.put(objectID, gameObjects);
        return gameObjects;
    }

    public List<DecorativeObject> findDecorativeObjectsByID(int objectID) {
        if (client.getLocalPlayer() == null) return new ArrayList<>();
        WorldView wv = client.getTopLevelWorldView();
        if (wv == null || wv.getScene() == null) return new ArrayList<>();

        int regionId = client.getLocalPlayer().getWorldLocation().getRegionID();
        checkAndClearCache(regionId * 10 + wv.getPlane());

        List<DecorativeObject> cached = decorativeObjectCache.get(objectID);
        if (cached != null) return cached;

        List<DecorativeObject> decorativeObjects = new ArrayList<>();
        Tile[][][] tiles = wv.getScene().getTiles();

        for (int z = 0; z < tiles.length; z++) {
            for (int x = 0; x < Constants.SCENE_SIZE; x++) {
                for (int y = 0; y < Constants.SCENE_SIZE; y++) {
                    Tile tile = tiles[z][x][y];
                    if (tile == null) continue;
                    DecorativeObject decorativeObject = tile.getDecorativeObject();
                    if (decorativeObject != null && objectIdMatches(decorativeObject.getId(), objectID)) {
                        decorativeObjects.add(decorativeObject);
                    }
                }
            }
        }
        decorativeObjectCache.put(objectID, decorativeObjects);
        return decorativeObjects;
    }

    public List<GroundObject> findGroundObjectsByID(int objectID) {
        if (client.getLocalPlayer() == null) return new ArrayList<>();
        WorldView wv = client.getTopLevelWorldView();
        if (wv == null || wv.getScene() == null) return new ArrayList<>();

        int regionId = client.getLocalPlayer().getWorldLocation().getRegionID();
        checkAndClearCache(regionId * 10 + wv.getPlane());

        List<GroundObject> cached = groundObjectCache.get(objectID);
        if (cached != null) return cached;

        List<GroundObject> groundObjects = new ArrayList<>();
        Tile[][][] tiles = wv.getScene().getTiles();

        for (int z = 0; z < tiles.length; z++) {
            for (int x = 0; x < Constants.SCENE_SIZE; x++) {
                for (int y = 0; y < Constants.SCENE_SIZE; y++) {
                    Tile tile = tiles[z][x][y];
                    if (tile == null) continue;
                    GroundObject groundObject = tile.getGroundObject();
                    if (groundObject != null && objectIdMatches(groundObject.getId(), objectID)) {
                        groundObjects.add(groundObject);
                    }
                }
            }
        }
        groundObjectCache.put(objectID, groundObjects);
        return groundObjects;
    }

    private boolean objectIdMatches(int sceneId, int targetId) {
        if (sceneId == targetId) return true;
        try {
            ObjectComposition comp = client.getObjectDefinition(sceneId);
            if (comp != null) {
                while (comp.getImpostor() != null) comp = comp.getImpostor();
                if (comp.getId() == targetId) return true;
            }
            comp = client.getObjectDefinition(targetId);
            if (comp != null) {
                while (comp.getImpostor() != null) comp = comp.getImpostor();
                if (comp.getId() == sceneId) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void drawTileObjectClickbox(Graphics2D graphics, TileObject tileObject, Color color) {
        if (tileObject == null) return;
        try {
            Shape shape = tileObject.getClickbox();
            if (shape == null) {
                if (tileObject instanceof GameObject) {
                    shape = ((GameObject) tileObject).getConvexHull();
                } else if (tileObject instanceof DecorativeObject) {
                    shape = ((DecorativeObject) tileObject).getConvexHull();
                } else if (tileObject instanceof GroundObject) {
                    shape = ((GroundObject) tileObject).getConvexHull();
                }
            }
            if (shape != null) {
                graphics.setColor(color);
                graphics.draw(shape);
                graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 5));
                graphics.fill(shape);
            }
        } catch (Exception e) {
            // Ignore transitioning errors
        }
    }

    // FIX: Simplified the rendering method so it plays nice with RuneLite's graphics engine
    public void renderHighlight(Graphics2D graphics, int objectId, Color color) {
        Client client = plugin.getClient();
        if (client == null) return;

        for (GameObject gameObject : findGameObjectsByID(objectId)) {
            drawTileObjectClickbox(graphics, gameObject, color);
        }
        for (DecorativeObject decorativeObject : findDecorativeObjectsByID(objectId)) {
            drawTileObjectClickbox(graphics, decorativeObject, color);
        }
        for (GroundObject groundObject : findGroundObjectsByID(objectId)) {
            drawTileObjectClickbox(graphics, groundObject, color);
        }
    }
}