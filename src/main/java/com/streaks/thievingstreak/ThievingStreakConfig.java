package com.streaks.thievingstreak;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("thievingstreak")
public interface ThievingStreakConfig extends Config
{
    @ConfigItem(
            keyName = "bestStreaks",
            name = "Best streaks",
            description = "Serialized best streaks map",
            hidden = true
    )
    default String bestStreaks()
    {
        return "";
    }
}
