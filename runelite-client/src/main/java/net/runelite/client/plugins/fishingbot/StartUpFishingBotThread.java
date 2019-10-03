package net.runelite.client.plugins.fishingbot;

public class StartUpFishingBotThread extends Thread {

    public StartUpFishingBotThread() {

    }

    @Override
    public void run() {
        try{
            Thread.sleep(10 * 600);
        } catch (InterruptedException ie) {

        }

        FishingBotPlugin.isStartUp = false;
    }
}
