package net.runelite.client.plugins.fishingbot;

import com.github.joonasvali.naturalmouse.api.MouseMotion;
import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;
import com.github.joonasvali.naturalmouse.util.FactoryTemplates;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

import static net.runelite.client.plugins.Plugin.isMouseMoving;

public class DropFishBotThread extends Thread {

    private MouseMotionFactory mouseMotionFactory;
    private ArrayList<MouseMotion> motions = new ArrayList<>();

    private Random random;
    private Robot robot;


    public DropFishBotThread(ArrayList<Integer> xPoints, ArrayList<Integer> yPoints){
        mouseMotionFactory = FactoryTemplates.createFastGamerMotionFactory();

        int size = xPoints.size();
        for(int i = 0; i < size; i++) {
            motions.add(mouseMotionFactory.build(xPoints.get(i), yPoints.get(i)));
        }

        try{
            this.robot = new Robot();
        } catch (AWTException awte){
            this.robot = null;
        }

        this.random = new Random();
    }


    @Override
    public void run() {

        if(isMouseMoving) {
            return;
        } else {
            isMouseMoving = true;
        }

        try{
            // sleep for awhile before executing mouse movements
            Thread.sleep(600 + (random.nextInt(2) * 600));
        } catch (InterruptedException ie){
            System.out.println("Error in move movement thread. Exiting");
            return;
        }

        for (int i = 0; i < motions.size(); i++) {
            try {

                // moving the mouse
                motions.get(i).move();

                ArrayList<Integer> buckets = splitToBuckets(125, 5);

                // shift clicking for dropping at random intervals
                Thread.sleep(25 + buckets.get(0));
                robot.keyPress(KeyEvent.VK_SHIFT);
                Thread.sleep(125 + buckets.get(1));
                robot.mousePress(InputEvent.getMaskForButton(1));
                Thread.sleep(175 + buckets.get(2));
                robot.mouseRelease(InputEvent.getMaskForButton(1));
                Thread.sleep(125 + buckets.get(3));
                robot.keyRelease(KeyEvent.VK_SHIFT);
                Thread.sleep(25 + buckets.get(4));

            } catch (InterruptedException ie) {
                System.out.println("Error in move movement thread. Exiting");
                return;
            }
        }

        try {
            // randomly sleep for awhile
            Thread.sleep(600 + (random.nextInt(4) * 600));
        } catch (InterruptedException ie) {
            System.out.println("Error in move movement thread. Exiting");
            return;
        }

        isMouseMoving = false;
    }

    private ArrayList<Integer> splitToBuckets(Integer bucket, Integer size) {
        ArrayList<Double> randomSeed = new ArrayList<>();
        ArrayList<Integer> newBuckets = new ArrayList<>();

        Double sum = 0.0;
        for(int i = 0; i < size; i ++){
            randomSeed.add(random.nextDouble());
            sum += randomSeed.get(i);
        }

        if(sum < 1.0) {
            for(int i = 0; i <randomSeed.size(); i++){
                newBuckets.add(bucket / 5);
            }
        } else {
            for(int i = 0; i <randomSeed.size(); i++){
                Double curr = randomSeed.get(i);
                curr = (curr / sum) * bucket;
                newBuckets.add(curr.intValue());
            }
        }
        return newBuckets;
    }
}
