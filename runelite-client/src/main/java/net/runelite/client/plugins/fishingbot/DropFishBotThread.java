package net.runelite.client.plugins.fishingbot;

import com.github.joonasvali.naturalmouse.api.MouseMotion;
import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;
import com.github.joonasvali.naturalmouse.util.FactoryTemplates;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

public class DropFishBotThread extends Thread {

    private MouseMotionFactory mouseMotionFactory;
    ArrayList<MouseMotion> motions = new ArrayList<>();
    Robot robot;

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
    }


    @Override
    public void run() {

        try{
            Thread.sleep(500);
        } catch (InterruptedException ie){
            System.out.println("error!!S!!");
        }

        for (int i = 0; i < motions.size(); i++) {
            try {

                // 2 ticks for movement -> 1.2 secs
                motions.get(i).move();

                ArrayList<Integer> buckets = splitToBuckets(125, 5);

                // 1 tick for dropping -> 0.6 secs
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
                System.out.println("error!!S!");
            }
        }
    }

    private ArrayList<Integer> splitToBuckets(Integer bucket, Integer size) {
        Random random = new Random();
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
