package com.streaks;

import com.streaks.thievingstreak.ThievingStreakPlugin;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ThievingStreakPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ThievingStreakPlugin.class);
		RuneLite.main(args);
	}
}