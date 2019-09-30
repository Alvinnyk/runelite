package net.runelite.client.bot;

import com.github.joonasvali.naturalmouse.api.MouseMotion;
import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;

import java.awt.*;

public class Bot {
    private MouseMotionFactory mouseMotionFactory;
    MouseMotion motion;
    Robot robot;

    public Bot(int x, int y){
        this.mouseMotionFactory = MouseMotionFactory.getDefault();
        motion = mouseMotionFactory.build(x, y);
        try{
            this.robot = new Robot();
        } catch (AWTException awte){
            this.robot = null;
        }
    }

    public void execute(){
        try{
            this.motion.move();
            try{
                robot = new Robot();
                /*Thread.sleep(200);
                robot.keyPress(KeyEvent.VK_SHIFT);
                Thread.sleep(200);
                robot.mousePress(InputEvent.getMaskForButton(1));
                Thread.sleep(200);
                robot.mouseRelease(InputEvent.getMaskForButton(1));
                Thread.sleep(200);
                robot.keyRelease(KeyEvent.VK_SHIFT);*/
            } catch (AWTException awte){
                System.out.println("move click failed");
            }
        } catch (InterruptedException ie) {
            System.out.println("move mouse failed");
        }
    }
}
