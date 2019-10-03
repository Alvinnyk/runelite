package net.runelite.client.plugins.fishingbot;

import com.github.joonasvali.naturalmouse.api.MouseMotion;
import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;
import com.github.joonasvali.naturalmouse.util.FactoryTemplates;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.Random;

import static net.runelite.client.plugins.Plugin.isMouseMoving;

public class FishingBotThread extends Thread {

    private MouseMotionFactory mouseMotionFactory;
    private MouseMotion motion;

    private Random random;
    private Robot robot;


    public FishingBotThread(int x, int y) {
        mouseMotionFactory = FactoryTemplates.createAverageComputerUserMotionFactory();
        motion = mouseMotionFactory.build(x, y);
        try{
            this.robot = new Robot();
        } catch (AWTException awte){
            this.robot = null;
        }

        this.random = new Random();
    }

    @Override
    public void run(){

        if(isMouseMoving) {
            return;
        } else {
            isMouseMoving = true;
        }

        try{

            Thread.sleep(600 + (random.nextInt(2) * 600));
            motion.move();

            Thread.sleep(50 + random.nextInt(50));
            robot.mousePress(InputEvent.getMaskForButton(1));
            Thread.sleep(200 + random.nextInt(50));
            robot.mouseRelease(InputEvent.getMaskForButton(1));

            Thread.sleep(600 + (random.nextInt(4) * 600));

        } catch (InterruptedException ie) {

        }

        isMouseMoving = false;
    }
}
