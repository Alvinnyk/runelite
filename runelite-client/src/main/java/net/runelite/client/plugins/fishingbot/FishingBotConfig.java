package net.runelite.client.plugins.fishingbot;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("fishing")
public interface FishingBotConfig extends Config {

    @ConfigItem(
            position = 0,
            keyName = "botDetails",
            name = "Bot Details",
            description = "Shows bot details"
    )
    default boolean botDetails() {return true;}

}
