package com.streaks;

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
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.Item;
import net.runelite.client.game.ItemManager;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.GameTick;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@PluginDescriptor(
        name = "Streak Tracker",
        description = "Tracks streaks for pickpocketing and farming harvests",
        tags = {"thieving", "farming", "streak"}
)
public class StreaksPlugin extends Plugin
{

    public enum SkillType
    {
        THIEVING,
        FARMING
    }

    public enum PatchType
    {
        HERB,
        ALLOTMENT
    }

    private static final Set<String> ALLOTMENT_ITEMS = new HashSet<>(Arrays.asList(
        "potato",
        "onion",
        "cabbage",
        "tomato",
        "sweetcorn",
        "strawberry",
        "watermelon",
        "snape grass"
    ));

    private static final Pattern PICKPOCKET_SUCCESS =
            Pattern.compile("You pick the (.+?)'s pocket\\.");

    private static final Pattern PICKPOCKET_FAIL =
            Pattern.compile("You fail to pick the (.+?)'s pocket\\.");

    private static final Pattern FARMING_HARVEST =
            Pattern.compile("You (?:harvest|pick|carefully pick) (?:some |a )?(.+?)(?:\\.|$)");

    private static final Pattern FARMING_DEPLETED =
            Pattern.compile("The patch is now empty\\.|You have finished harvesting this patch\\.");

    private static final Pattern PATCH_START =
        Pattern.compile("You begin to harvest the (herb patch|allotment)\\.", Pattern.CASE_INSENSITIVE);

    private static final Pattern PATCH_EMPTY =
        Pattern.compile("The (herb patch|allotment) is now empty\\.", Pattern.CASE_INSENSITIVE);
    
    private static final Type MAP_TYPE = new TypeToken<Map<String, Integer>>() {}.getType();
    private static final int STREAK_TIMEOUT_TICKS = 50; // 30 seconds

    @Inject
    @Getter
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private Gson gson;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private StreaksOverlay overlay;

    @Inject
    private StreaksPanel panel;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private StreaksConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private NewBestOverlay newBestOverlay;

    @Getter
    private SkillType activeSkill;

    @Getter
    private String activeTarget;

    @Getter
    private int currentStreak;

    @Getter
    private Map<String, Integer> bestThievingStreaks = new HashMap<>();

    @Getter
    private Map<String, Integer> bestFarmingStreaks = new HashMap<>();

    @Getter
    private boolean celebrationActive = false;

    @Getter
    private long celebrateStartMillis;

    @Getter
    private SkillType celebrateSkill;

    @Getter
    private String celebrateTarget;

    @Getter
    private int celebrateValue;

    private NavigationButton navButton;
    private boolean patchHarvestActive = false;
    private PatchType currentPatchType = null;
    private int patchItemId = -1;
    private int lastFarmingXpTick = -1;
    private int streakTimeoutTick = -1;

    private final Map<Integer, Integer> lastInventory = new HashMap<>();

    public void clearCelebration()
    {
        celebrationActive = false;
    }

    @Provides
    StreaksConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(StreaksConfig.class);
    }

    @Override
    protected void startUp()
    {
        lastInventory.clear();
        patchHarvestActive = false;
        patchItemId = -1;
        lastFarmingXpTick = -1;
        currentPatchType = null;
        loadBestStreaks();

        overlayManager.add(overlay);
        overlayManager.add(newBestOverlay);

        final BufferedImage icon = ImageUtil.loadImageResource(StreaksPlugin.class, "icon.png");

        navButton = NavigationButton.builder()
                .tooltip("Streak Tracker")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);

        panel.updateCurrent(null, "", 0);
        panel.updateThievingBest(bestThievingStreaks);
        panel.updateFarmingBest(bestFarmingStreaks);
    }

    @Override
    protected void shutDown()
    {
        finishCurrentStreak(); // commit current streak before shutdown
        overlayManager.remove(overlay);
        overlayManager.remove(newBestOverlay);
        clearCelebration();

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

        Matcher m;

        // Thieving
        m = PICKPOCKET_SUCCESS.matcher(message);
        if (m.matches())
        {
            String npc = m.group(1);
            handleThievingSuccess(npc);
            return;
        }

        m = PICKPOCKET_FAIL.matcher(message);
        if (m.matches())
        {
            String npc = m.group(1);
            handleThievingFailure(npc);
            return;
        }

        // Farming
        m = FARMING_HARVEST.matcher(message);
        if (m.matches())
        {
            String crop = m.group(1).trim();
            handleFarmingHarvest(crop);
            return;
        }

        m = FARMING_DEPLETED.matcher(message);
        if (m.matches())
        {
            handleFarmingDepleted();
        }

        m = PATCH_START.matcher(message);
        if (m.matches())
        {
            String patchToken = m.group(1); // "herb patch" or "allotment"
            startPatchHarvest(patchToken);
            return;
        }

        m = PATCH_EMPTY.matcher(message);
        if (m.matches())
        {
            // Invoke next tick so that we still count the final item obtained
            clientThread.invokeLater(this::endPatchHarvest);
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        GameState state = event.getGameState();
        if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING || state == GameState.CONNECTION_LOST)
        {
            finishCurrentStreak();
            lastInventory.clear();
            patchHarvestActive = false;
            patchItemId = -1;
            lastFarmingXpTick = -1;
            currentPatchType = null;
        }
    }

    // TODO refactor with farming to make something generic
    private void handleThievingSuccess(String npc)
    {
        if (activeSkill != SkillType.THIEVING || activeTarget == null || !activeTarget.equals(npc))
        {
            finishCurrentStreak();
            activeSkill = SkillType.THIEVING;
            activeTarget = npc;
            currentStreak = 0;
        }

        currentStreak++;
        resetStreakTimer();
        panel.updateCurrent(activeSkill, activeTarget, currentStreak);
    }

    private void handleThievingFailure(String npc)
    {
        if (activeSkill == SkillType.THIEVING && activeTarget != null && activeTarget.equals(npc))
        {
            finishCurrentStreak();
        }
    }

    private void handleFarmingHarvest(String crop)
    {
        if (activeSkill != SkillType.FARMING || activeTarget == null || !activeTarget.equals(crop))
        {
            finishCurrentStreak();
            activeSkill = SkillType.FARMING;
            activeTarget = crop;
            currentStreak = 0;
        }

        currentStreak++;
        resetStreakTimer();
        panel.updateCurrent(activeSkill, activeTarget, currentStreak);
    }

    private void handleFarmingDepleted()
    {
        if (activeSkill == SkillType.FARMING)
        {
            finishCurrentStreak();
        }
    }

    private void startPatchHarvest(String patchToken)
    {
        // If we’re already harvesting herbs on this patch, do nothing
        if (patchHarvestActive && activeSkill == SkillType.FARMING)
        {
            return;
        }

        finishCurrentStreak();

        activeSkill = SkillType.FARMING;
        activeTarget = null; // unknown until we see which item
        currentStreak = 0;

        patchHarvestActive = true;
        patchItemId = -1;
        lastFarmingXpTick = -1;

        patchToken = patchToken.toLowerCase();
        if (patchToken.startsWith("herb"))
        {
            currentPatchType = PatchType.HERB;
            panel.updateCurrent(activeSkill, "Herb patch", 0);
        }
        else
        {
            currentPatchType = PatchType.ALLOTMENT;
            panel.updateCurrent(activeSkill, "Allotment", 0);
        }
    }

    private void endPatchHarvest()
    {
        if (!patchHarvestActive)
        {
            return;
        }

        patchHarvestActive = false;
        patchItemId = -1;
        lastFarmingXpTick = -1;
        currentPatchType = null;
        finishCurrentStreak();
    }


    private void finishCurrentStreak()
    {
        if (activeSkill == null || activeTarget == null || currentStreak <= 0)
        {
            activeSkill = null;
            activeTarget = null;
            currentStreak = 0;
            panel.updateCurrent(null, "", 0);
            return;
        }

        switch (activeSkill)
        {
            case THIEVING:
            {
                int best = bestThievingStreaks.getOrDefault(activeTarget, 0);
                if (currentStreak > best)
                {
                    bestThievingStreaks.put(activeTarget, currentStreak);
                    triggerCelebration(SkillType.THIEVING, activeTarget, currentStreak);
                    saveThievingBestStreaks();
                }
                panel.updateThievingBest(bestThievingStreaks);
                break;
            }
            case FARMING:
            {
                int best = bestFarmingStreaks.getOrDefault(activeTarget, 0);
                if (currentStreak > best)
                {
                    bestFarmingStreaks.put(activeTarget, currentStreak);
                    triggerCelebration(SkillType.FARMING, activeTarget, currentStreak);
                    saveFarmingBestStreaks();
                }
                panel.updateFarmingBest(bestFarmingStreaks);
                break;
            }
        }

        activeSkill = null;
        activeTarget = null;
        currentStreak = 0;
        streakTimeoutTick = -1;
        panel.updateCurrent(null, "", 0);
    }

    private Map<String, Integer> loadMap(String json)
    {
        if (json == null || json.isEmpty())
        {
            return new HashMap<>();
    }

        try
        {
            Map<String, Integer> map = gson.fromJson(json, MAP_TYPE);
            return map != null ? map : new HashMap<>();
        }
        catch (Exception e)
        {
            return new HashMap<>();
        }
    }

    private void loadBestStreaks()
    {
        bestThievingStreaks = loadMap(config.bestThievingStreaks());
        bestFarmingStreaks = loadMap(config.bestFarmingStreaks());
    }

    private void saveThievingBestStreaks()
    {
        String json = gson.toJson(bestThievingStreaks);
        configManager.setConfiguration("streaks", "bestThievingStreaks", json);
    }

    private void saveFarmingBestStreaks()
    {
        String json = gson.toJson(bestFarmingStreaks);
        configManager.setConfiguration("streaks", "bestFarmingStreaks", json);
    }

    public void deleteStreak(SkillType skill, String key)
    {
        if (skill == null || key == null)
        {
            return;
        }

        switch (skill)
        {
            case THIEVING:
                if (bestThievingStreaks.remove(key) != null)
                {
                    saveThievingBestStreaks();
                    panel.updateThievingBest(bestThievingStreaks);
                }
                break;
            case FARMING:
                if (bestFarmingStreaks.remove(key) != null)
                {
                    saveFarmingBestStreaks();
                    panel.updateFarmingBest(bestFarmingStreaks);
                }
                break;
        }
    }

    public void resetAllStreaks()
    {
        bestThievingStreaks.clear();
        bestFarmingStreaks.clear();
        saveThievingBestStreaks();
        saveFarmingBestStreaks();

        activeSkill = null;
        activeTarget = null;
        currentStreak = 0;

        panel.updateCurrent(null, "", 0);
        panel.updateThievingBest(bestThievingStreaks);
        panel.updateFarmingBest(bestFarmingStreaks);
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (event.getContainerId() != InventoryID.INVENTORY.getId())
        {
            return;
        }

        ItemContainer container = event.getItemContainer();
        if (container == null)
        {
            return;
        }

        // Build current inventory counts
        Map<Integer, Integer> current = new HashMap<>();
        for (Item item : container.getItems())
        {
            if (item == null || item.getId() <= 0)
            {
                continue;
            }
            current.merge(item.getId(), item.getQuantity(), Integer::sum);
        }

        if (patchHarvestActive
            && activeSkill == SkillType.FARMING
            && client.getTickCount() == lastFarmingXpTick
            && currentPatchType != null)
        {
            for (Map.Entry<Integer, Integer> e : current.entrySet())
            {
                int id = e.getKey();
                int newQty = e.getValue();
                int oldQty = lastInventory.getOrDefault(id, 0);
                int delta = newQty - oldQty;

                if (delta <= 0)
                {
                    continue;
                }

                // If we don't yet know which iteme this patch is, try to identify it
                if (patchItemId == -1)
                {
                    String name = itemManager.getItemComposition(id).getName();
                    String lower = name.toLowerCase();

                    if (currentPatchType == PatchType.HERB)
                    {
                        if (lower.contains("grimy"))
                        {
                            patchItemId = id;
                            activeTarget = name;
                        }
                    }
                    else if (currentPatchType == PatchType.ALLOTMENT)
                    {
                        // exact match against allotment list
                        if (ALLOTMENT_ITEMS.contains(lower))
                        {
                            patchItemId = id;
                            activeTarget = name;
                        }
                    }
                }

                // Count only the chosen herb type
                if (id == patchItemId && patchItemId != -1)
                {
                    currentStreak += delta;
                    resetStreakTimer();
                    String label;
                    if (activeTarget != null && !activeTarget.isEmpty())
                    {
                        label = activeTarget;
                    }
                    else
                    {
                        label = (currentPatchType == PatchType.HERB) ? "Herb patch" : "Allotment";
                    }

                    panel.updateCurrent(activeSkill, label, currentStreak);
                }
            }
        }

        lastInventory.clear();
        lastInventory.putAll(current);
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        if (event.getSkill() != Skill.FARMING)
        {
            return;
        }

        if (!patchHarvestActive || activeSkill != SkillType.FARMING)
        {
            return;
        }

        // Mark this tick as a "Farming XP tick" during an active herb harvest
        lastFarmingXpTick = client.getTickCount();
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (activeSkill == null)
        {
            return;
        }

        if (streakTimeoutTick > 0 && client.getTickCount() >= streakTimeoutTick)
        {
            // Timer expired → streak ends
            finishCurrentStreak();
            streakTimeoutTick = -1;
        }
    }

    private void resetStreakTimer()
    {
        streakTimeoutTick = client.getTickCount() + STREAK_TIMEOUT_TICKS;
    }

    protected double getSecondsRemainingInStreak()
    {
        int tick = client.getTickCount();
        int until = streakTimeoutTick;
        double seconds = 0;
        if (until > 0)
        {
            int remainingTicks = until - tick;
            if (remainingTicks < 0)
            {
                remainingTicks = 0;
            }

            seconds = remainingTicks * 0.6; // 1 tick = 0.6s
        }

        return seconds;
    }

    private void triggerCelebration(SkillType skill, String target, int value)
    {
        celebrationActive = true;
        celebrateStartMillis = System.currentTimeMillis();
        celebrateSkill = skill;
        celebrateTarget = target;
        celebrateValue = value;
    }


}
