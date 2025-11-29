package com.streaks.thievingstreak;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("thievingstreak")
public interface ThievingStreakConfig extends Config
{
    @ConfigItem(
            keyName = "bestStreaks",
            name = "Best Thieving streaks",
            description = "Serialized best thieving streaks map",
            hidden = true
    )
    default String bestThievingStreaks()
    {
        return "";
    }

    @ConfigItem(
            keyName = "bestFarmingStreaks",
            name = "Best Farming streaks",
            description = "Serialized best farming streaks map",
            hidden = true
    )
    default String bestFarmingStreaks()
    {
        return "";
    }
}
