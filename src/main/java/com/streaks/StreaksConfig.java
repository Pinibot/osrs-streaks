package com.streaks;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("streaks")
public interface StreaksConfig extends Config
{
    @ConfigItem(
            keyName = "bestThievingStreaks",
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

    @ConfigItem(
            keyName = "bestHunterStreaks",
            name = "Best Hunter streaks",
            description = "Serialized best hunter streaks map",
            hidden = true
    )
    default String bestHunterStreaks()
    {
        return "";
    }

    
    @ConfigItem(
        keyName = "showStreakOverlay",
        name = "Show current streak overlay",
        description = "Show the current streak overlay on screen"
    )
    default boolean showStreakOverlay()
    {
        return true;
    }

    @ConfigItem(
        keyName = "showCelebrationOverlay",
        name = "Show celebration overlay",
        description = "Show the celebration overlay when you set a new best"
    )
    default boolean showCelebrationOverlay()
    {
        return true;
    }

    @ConfigItem(
        keyName = "takeScreenshotOnNewBest",
        name = "Take screenshot on new best",
        description = "If selected, take a new screenshot and save it to .runelite/streaks"
    )
    default boolean takeScreenshotOnNewBest()
    {
        return true;
    }
}
