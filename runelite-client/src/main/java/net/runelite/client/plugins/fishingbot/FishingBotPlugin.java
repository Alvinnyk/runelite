package net.runelite.client.plugins.fishingbot;

import com.google.inject.Provides;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@PluginDescriptor(
        name = "Fishing Bot",
        description = "Fishing Bot"
)
public class FishingBotPlugin extends Plugin {

    private final List<NPC> fishingSpots = new ArrayList<>();
    @Inject
    ClientUI clientUI;
    private NPC currentSpot;
    private Player player;

    private int gameTick = 0;
    private int botTick = 0;

    private Integer fishingLevel;

    private int inventorySize;

    private int isUnstableHeuristic;
    private boolean isStable;
    public static boolean isStartUp;

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private FishingBotConfig config;

    @Inject
    private FishingBotOverlay overlay;

    @Provides
    FishingBotConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FishingBotConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);

        player = client.getLocalPlayer();
        fishingSpots.clear();
        currentSpot = null;
        fishingLevel = client.getRealSkillLevel(Skill.FISHING);

        isStartUp = true;
        isUnstableHeuristic = 0;
        isStable = true;

        // wait 10 ticks -> 6 seconds
        StartUpFishingBotThread startUp = new StartUpFishingBotThread();
        startUp.start();
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);

        player = null;
        fishingSpots.clear();
        currentSpot = null;
        fishingLevel = null;
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {

        int containerId = event.getContainerId();
        if (containerId == InventoryID.INVENTORY.getId()) {

            // checks the number of items inside inventory
            Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
            Collection<WidgetItem> widgetItems = inventoryWidget.getWidgetItems();
            inventorySize = widgetItems.size();
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {

        if(isStartUp) {
            return;
        }

        gameTick++;
        if (gameTick % 10 != 0 || !isStable) {
            return;
        }
        gameTick = 0;
        botTick++;

        System.out.println("Unstable heuristic: " + isUnstableHeuristic);

        // check stability
        if(currentSpot != null) {
            isUnstableHeuristic = 0;
        } else if (isUnstableHeuristic > 4){
            isStable = false;
        }

        // if inventory is filled, drop fishes
        if (inventorySize >= 28) {
            dropFishes();
            return;
        }

        // checks if u are currently interacting with a fishing spot
        // Also checks if u have leveled up and re clicks the fishing spot
        if (currentSpot == null || fishingLevel != client.getRealSkillLevel(Skill.FISHING)) {

            if(!isMouseMoving) {
                isUnstableHeuristic ++;
            }

            fishingLevel = client.getRealSkillLevel(Skill.FISHING);
            clickFishingSpot();

        }
    }

    private void dropFishes() {

        // do not move mouse if it is already moving
        if(isMouseMoving) {
            return;
        }

        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        Collection<WidgetItem> widgetItems = inventoryWidget.getWidgetItems();

        Random random = new Random();
        ArrayList<Integer> xPoints = new ArrayList<>();
        ArrayList<Integer> yPoints = new ArrayList<>();

        // generate coordinates of items that needs to be dropped
        Iterator<WidgetItem> iterator = widgetItems.iterator();
        while (iterator.hasNext()) {
            WidgetItem currentItem = iterator.next();
            if (currentItem.getId() == ItemID.RAW_TROUT ||
                    currentItem.getId() == ItemID.RAW_SALMON ||
                    currentItem.getId() == ItemID.CLUE_BOTTLE_BEGINNER ||
                    currentItem.getId() == ItemID.RAW_SHRIMPS ||
                    currentItem.getId() == ItemID.RAW_ANCHOVIES) {

                Rectangle bounds = currentItem.getCanvasBounds();

                int xOffset = clientUI.frame.getX() + clientUI.getCanvasOffset().getX();
                int yOffset = clientUI.frame.getY() + clientUI.getCanvasOffset().getY();

                int xBound = (int) bounds.getX() + (int) (bounds.getWidth() * 0.25) + random.nextInt((int) (bounds.getWidth() * 0.5));
                int yBound = (int) bounds.getY() + (int) (bounds.getHeight() * 0.25) + random.nextInt((int) (bounds.getHeight() * 0.5));

                int x = xOffset + xBound;
                int y = yOffset + yBound;

                xPoints.add(x);
                yPoints.add(y);
            }
        }
        DropFishBotThread botThread = new DropFishBotThread(xPoints, yPoints);
        botThread.start();
    }

    // Checks if there is a fishing spot, and click it
    private void clickFishingSpot() {

        if (fishingSpots.size() == 0) {
            System.out.println("no fishing spot");
            return;
        }

        if(isMouseMoving) {
            return;
        }

        inverseSortSpotDistanceFromPlayer();
        NPC targetSpot = fishingSpots.get(fishingSpots.size() - 1);

        // Get shape of fishing spot
        Shape shape = targetSpot.getConvexHull();
        if (shape == null) {
            return;
        }

        Random random = new Random();
        Rectangle rectangle = shape.getBounds();

        // randomly picks a point within the boundary of the rectangle
        int xPoint = (int) rectangle.getX() + (int) (0.3 * rectangle.getWidth()) + (int) (0.4 * random.nextDouble() * rectangle.getWidth());
        int yPoint = (int) rectangle.getY() + (int) (0.3 * rectangle.getHeight()) + (int) (0.4 * random.nextDouble() * rectangle.getHeight());

        int x;
        int y;

        // check if this generated point is within the boundary
        if (shape.contains(xPoint, yPoint)) {
            x = clientUI.frame.getX() + xPoint + clientUI.getCanvasOffset().getX();
            y = clientUI.frame.getY() + yPoint + clientUI.getCanvasOffset().getY();
        } else {
            System.out.println("Does not contain randomly generated point");
            return;
        }

        System.out.println("Attempting to fish at " + targetSpot.getId());

        FishingBotThread botThread = new FishingBotThread(x, y);
        botThread.start();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        if (gameState == GameState.CONNECTION_LOST || gameState == GameState.LOGIN_SCREEN || gameState == GameState.HOPPING) {
            fishingSpots.clear();
            currentSpot = null;
        }
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event) {

        // interactor is not player
        if (event.getSource() != client.getLocalPlayer()) {
            return;
        }

        final Actor target = event.getTarget();

        // player is not interacting
        if (target == null) {
            currentSpot = null;
            return;
        }

        // target is not NPC
        if (!(target instanceof NPC)) {
            currentSpot = null;
            return;
        }

        final NPC npc = (NPC) target;
        FishingSpot spot = FishingSpot.findSpot(npc.getId());

        // target is not a fishing spot
        if (spot == null) {
            currentSpot = null;
            return;
        }

        currentSpot = npc;
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        final NPC npc = event.getNpc();

        // npc is not a fishing spot
        if (FishingSpot.findSpot(npc.getId()) == null) {
            return;
        }

        fishingSpots.add(npc);
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        final NPC npc = npcDespawned.getNpc();

        fishingSpots.remove(npc);
    }


    private void inverseSortSpotDistanceFromPlayer() {
        if (fishingSpots.isEmpty()) {
            return;
        }

        final LocalPoint cameraPoint = new LocalPoint(client.getCameraX(), client.getCameraY());
        fishingSpots.sort(
                Comparator.comparing(
                        // Negate to have the furthest first
                        (NPC npc) -> -npc.getLocalLocation().distanceTo(cameraPoint))
                        // Order by position
                        .thenComparing(NPC::getLocalLocation, Comparator.comparing(LocalPoint::getX)
                                .thenComparing(LocalPoint::getY))
                        // And then by id
                        .thenComparing(NPC::getId)
        );
    }

    public int getBotTick() {
        return this.botTick;
    }

    public boolean getIsMouseMoving() {
        return isMouseMoving;
    }

    public int getInventorySize() {
        return inventorySize;
    }

    public boolean getIsStable() {
        return isStable;
    }

    public boolean getIsStartUp() {
        return isStartUp;
    }

}
