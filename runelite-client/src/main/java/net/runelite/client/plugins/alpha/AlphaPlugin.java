package net.runelite.client.plugins.alpha;

import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;

@PluginDescriptor(
        name = "Alpah",
        description = "Attacks Guards"
)
public class AlphaPlugin extends Plugin {

    @Inject
    ClientUI clientUI;

    public static int counter = 0;

    private ArrayList<NPC> npc = new ArrayList<>();
    public static MouseMotionFactory currentMouseMotionFactory;

    @Subscribe
    void onNpcSpawned(NpcSpawned event) {
        int id = event.getNpc().getId();
        if(id == 3011 || id == 3010){
            if(!npc.contains(event.getNpc())){
                this.npc.add(event.getNpc());
            }
        }
    }

    @Subscribe
    void onPlayerSpawned(PlayerSpawned event){
        Actor player = event.getActor();
        String playerName = player.getName();
        int playerLevel = player.getCombatLevel();

        System.out.println("Lvl: " + playerLevel + " " + playerName);
    }

    @Subscribe
    void onGameTick(GameTick event){
        counter ++;
        if(counter % 20 == 0){
            System.out.println("Number of NPC in arraylist: " + npc.size());
            for(int i = 0; i < npc.size(); i++){
                if(!npc.get(i).isDead()){
                    NPC currentNpc = npc.get(i);
                    if(currentNpc.getConvexHull() != null){
                        /*Polygon polygon = currentNpc.getConvexHull();

                        int size = polygon.npoints;
                        int x = clientUI.frame.getX() + polygon.xpoints[size/2] + clientUI.getCanvasOffset().getX();
                        int y = clientUI.frame.getY() + polygon.ypoints[size/2] + clientUI.getCanvasOffset().getY();

                        System.out.println("Attack guard: " + currentNpc.getId());
                        RunnableAttackGuard runnableAttackGuard = new RunnableAttackGuard(x, y);
                        Thread thread = new Thread(runnableAttackGuard);
                        thread.start();
                        break;*/
                    }
                }
            }
        }
    }
}
