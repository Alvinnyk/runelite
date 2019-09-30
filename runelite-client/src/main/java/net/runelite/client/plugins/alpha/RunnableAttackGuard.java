package net.runelite.client.plugins.alpha;

import net.runelite.client.bot.Bot;

public class RunnableAttackGuard implements Runnable {
    private Bot bot;

    public RunnableAttackGuard(int x, int y){
        bot = new Bot(x, y);
    }

    @Override
    public void run() {
        bot.execute();
    }
}
