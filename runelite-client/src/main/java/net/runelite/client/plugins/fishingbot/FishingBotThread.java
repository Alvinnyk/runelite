package net.runelite.client.plugins.fishingbot;

import com.github.joonasvali.naturalmouse.api.MouseMotion;
import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;
import com.github.joonasvali.naturalmouse.util.FactoryTemplates;

import java.awt.*;
import java.awt.event.InputEvent;

public class FishingBotThread extends Thread {

    private MouseMotionFactory mouseMotionFactory;
    MouseMotion motion;
    Robot robot;


    public FishingBotThread(int x, int y) {
        mouseMotionFactory = FactoryTemplates.createAverageComputerUserMotionFactory();
        motion = mouseMotionFactory.build(x, y);
        try{
            this.robot = new Robot();
        } catch (AWTException awte){
            this.robot = null;
        }
    }

    @Override
    public void run(){

        try{

            motion.move();

            Thread.sleep(50);
            robot.mousePress(InputEvent.getMaskForButton(1));
            Thread.sleep(200);
            robot.mouseRelease(InputEvent.getMaskForButton(1));


        } catch (InterruptedException ie) {

        }

    }
}
