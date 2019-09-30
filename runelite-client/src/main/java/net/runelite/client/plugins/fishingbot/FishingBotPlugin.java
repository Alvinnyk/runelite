package net.runelite.client.plugins.fishingbot;

import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ExperienceChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;

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

    private FishingSpot currentSpot;

    private Player player;

    private int counter = 0;

    private int semaphore = 0;

    private int botTick = 0;

    private Integer fishingLevel;

    @Inject
    private Client client;

    @Inject
    ClientUI clientUI;

    @Override
    protected void startUp() throws Exception {
        player = client.getLocalPlayer();
        currentSpot = null;
        fishingLevel = client.getRealSkillLevel(Skill.FISHING);
    }

    @Override
    protected void shutDown() throws Exception {
        fishingSpots.clear();
        currentSpot = null;
        fishingLevel = null;
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {

        if(semaphore > 0) {
            return;
        }

        int containerId = event.getContainerId();

        if(containerId == InventoryID.INVENTORY.getId()){

            //dropFishes(28);
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {

        counter++;

        if(semaphore > 0) {
            semaphore --;
            System.out.println("Semaphore = " + semaphore);
            return;
        }

        // exit
        if (counter % 10 != 0) {
            return;
        }
        counter = 0;
        System.out.println("Bot tick " + botTick);
        botTick++;

        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        Collection<WidgetItem> widgetItems = inventoryWidget.getWidgetItems();
        if(widgetItems.size() >= 28) {
            dropFishes(28);
            return;
        }

        if(currentSpot != null) {
            System.out.println("Current fishing spot: " + "ID: " + currentSpot.getName() + " " + "Number of spots: " + fishingSpots.size());
        } else {
            System.out.println("Current fishing spot: NULL");
        }

        if(currentSpot == null || fishingLevel != client.getRealSkillLevel(Skill.FISHING)) {
            fishingLevel = client.getRealSkillLevel(Skill.FISHING);
            clickFishingSpot();
        }
    }

    private void dropFishes(int threshold) {

        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        Collection<WidgetItem> widgetItems = inventoryWidget.getWidgetItems();

        if(widgetItems.size() >= threshold) {

            Random random = new Random();

            ArrayList<Integer> xPoints = new ArrayList<>();
            ArrayList<Integer> yPoints = new ArrayList<>();

            Iterator<WidgetItem> iterator = widgetItems.iterator();
            while (iterator.hasNext()){
                WidgetItem currentItem = iterator.next();
                if(currentItem.getId() == ItemID.RAW_TROUT ||
                        currentItem.getId() == ItemID.RAW_SALMON ||
                        currentItem.getId() == ItemID.CLUE_BOTTLE_BEGINNER){

                    semaphore += 2; // 1.2 secs

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
            semaphore += 2;     //1.2 secs
            semaphore += random.nextInt(3);

            DropFishBotThread botThread = new DropFishBotThread(xPoints, yPoints);
            botThread.start();
        }
    }

    // Checks if there is a fishing spot, and click it
    private void clickFishingSpot() {

        if(fishingSpots.size() == 0) {
            System.out.println("no fishing spot");
            return;
        }

        inverseSortSpotDistanceFromPlayer();

        NPC targetSpot = fishingSpots.get(fishingSpots.size() - 1);

        // Get polygon of fishing spot
        Polygon polygon = targetSpot.getConvexHull();
        if(polygon == null) {
            return;
        }

        Random random = new Random();
        Rectangle rectangle = polygon.getBounds();

        int xPoint = (int) rectangle.getX() + (int)(0.45 * rectangle.getWidth()) + (int)(0.1 * random.nextDouble() * rectangle.getWidth());
        int yPoint = (int) rectangle.getY() + (int)(0.45 * rectangle.getHeight()) + (int)(0.1 * random.nextDouble() * rectangle.getHeight());

        int x;
        int y;

        if(polygon.contains(xPoint, yPoint)) {
            x = clientUI.frame.getX() + xPoint + clientUI.getCanvasOffset().getX();
            y = clientUI.frame.getY() + yPoint + clientUI.getCanvasOffset().getY();
        } else {
            System.out.println("Does not contain randomly generated point");
            return;
        }

        System.out.println("Attempting to fish at " + targetSpot.getId());

        semaphore += 2;
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
        if(target == null){
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

        currentSpot = spot;
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

}
