package com.streaks.thievingstreak;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.api.ChatMessageType;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PluginDescriptor(
        name = "Thieving Streaks",
        description = "Tracks pickpocket streaks per NPC type",
        tags = {"thieving", "pickpocket", "streak"}
)
public class ThievingStreakPlugin extends Plugin
{
    private static final Pattern PICKPOCKET_SUCCESS =
            Pattern.compile("You pick the (.+?)'s pocket\\.");

    private static final Pattern PICKPOCKET_FAIL =
            Pattern.compile("You fail to pick the (.+?)'s pocket\\.");

    private static final String CONFIG_GROUP = "thievingstreak";
    private static final String CONFIG_KEY_BEST = "bestStreaks";

    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Integer>>() {}.getType();

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ThievingStreakOverlay overlay;

    @Inject
    private ThievingStreakPanel panel;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ThievingStreakConfig config;

    @Inject
    private ConfigManager configManager;

    @Getter
    private String activeNpc;

    @Getter
    private int currentStreak;

    @Getter
    private Map<String, Integer> bestStreaks = new HashMap<>();

    private NavigationButton navButton;

    @Provides
    ThievingStreakConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ThievingStreakConfig.class);
    }

    @Override
    protected void startUp()
    {
        loadBestStreaks();

        overlayManager.add(overlay);

        final BufferedImage icon = ImageUtil.loadImageResource(ThievingStreakPlugin.class, "thieving_icon.png"); // optional, or null

        navButton = NavigationButton.builder()
                .tooltip("Thieving streaks")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);

        panel.updateCurrent("", 0);
        panel.updateBestStreaks(bestStreaks);
    }

    @Override
    protected void shutDown()
    {
        finishCurrentStreak(); // commit current streak before shutdown
        overlayManager.remove(overlay);

        if (navButton != null)
        {
            clientToolbar.removeNavigation(navButton);
            navButton = null;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        ChatMessageType type = event.getType();
        if (type != ChatMessageType.SPAM && type != ChatMessageType.GAMEMESSAGE)
        {
            return;
        }

        String message = Text.removeTags(event.getMessage());

        Matcher successMatcher = PICKPOCKET_SUCCESS.matcher(message);
        Matcher failMatcher = PICKPOCKET_FAIL.matcher(message);

        if (successMatcher.matches())
        {
            String npc = successMatcher.group(1);
            handleSuccess(npc);
        }
        else if (failMatcher.matches())
        {
            String npc = failMatcher.group(1);
            handleFailure(npc);
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        GameState state = event.getGameState();
        if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING || state == GameState.CONNECTION_LOST)
        {
            // Logging out / hopping ends the streak
            finishCurrentStreak();
        }
    }

    private void handleSuccess(String npc)
    {
        // If you change NPC type, start a new streak for that type
        if (activeNpc == null || !activeNpc.equals(npc))
        {
            finishCurrentStreak(); // commit previous NPC streak before switching
            activeNpc = npc;
            currentStreak = 0;
        }

        currentStreak++;

        panel.updateCurrent(activeNpc, currentStreak);
        // Overlay reads from getters, so nothing else needed
    }

    private void handleFailure(String npc)
    {
        // Treat any failure as end of current streak if it matches the active NPC
        if (activeNpc != null && activeNpc.equals(npc))
        {
            finishCurrentStreak();
        }
    }

    private void finishCurrentStreak()
    {
        if (activeNpc == null || currentStreak <= 0)
        {
            activeNpc = null;
            currentStreak = 0;
            panel.updateCurrent("", 0);
            return;
        }

        int best = bestStreaks.getOrDefault(activeNpc, 0);
        if (currentStreak > best)
        {
            bestStreaks.put(activeNpc, currentStreak);
            saveBestStreaks();
        }

        activeNpc = null;
        currentStreak = 0;

        panel.updateCurrent("", 0);
        panel.updateBestStreaks(bestStreaks);
    }

    private void loadBestStreaks()
    {
        String json = config.bestStreaks();
        if (json == null || json.isEmpty())
        {
            bestStreaks = new HashMap<>();
            return;
        }

        try
        {
            bestStreaks = GSON.fromJson(json, MAP_TYPE);
            if (bestStreaks == null)
            {
                bestStreaks = new HashMap<>();
            }
        }
        catch (Exception e)
        {
            bestStreaks = new HashMap<>();
        }
    }

    private void saveBestStreaks()
    {
        String json = GSON.toJson(bestStreaks);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_BEST, json);
    }
}
