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
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.Item;
import net.runelite.client.game.ItemManager;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PluginDescriptor(
        name = "Streak Tracker",
        description = "Tracks streaks for pickpocketing and farming harvests",
        tags = {"thieving", "farming", "streak"}
)
public class ThievingStreakPlugin extends Plugin
{

    public enum SkillType
    {
        THIEVING,
        FARMING
    }

    private static final Pattern PICKPOCKET_SUCCESS =
            Pattern.compile("You pick the (.+?)'s pocket\\.");

    private static final Pattern PICKPOCKET_FAIL =
            Pattern.compile("You fail to pick the (.+?)'s pocket\\.");

    // TODO check the actual strings for this
    private static final Pattern FARMING_HARVEST =
            Pattern.compile("You (?:harvest|pick|carefully pick) (?:some |a )?(.+?)(?:\\.|$)");

    private static final Pattern FARMING_DEPLETED =
            Pattern.compile("The patch is now empty\\.|You have finished harvesting this patch\\.");

    private static final Pattern HERB_START =
        Pattern.compile("You begin to harvest the herb patch\\.", Pattern.CASE_INSENSITIVE);

    private static final Pattern HERB_EMPTY =
        Pattern.compile("The herb patch is now empty\\.", Pattern.CASE_INSENSITIVE);
    
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

    @Inject
    private ItemManager itemManager;

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

    private NavigationButton navButton;
    private boolean herbHarvestActive = false;
    private int herbItemId = -1;
    private int lastFarmingXpTick = -1;

    private final Map<Integer, Integer> lastInventory = new HashMap<>();

    @Provides
    ThievingStreakConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ThievingStreakConfig.class);
    }

    @Override
    protected void startUp()
    {
        lastInventory.clear();
        herbHarvestActive = false;
        herbItemId = -1;
        lastFarmingXpTick = -1;
        loadBestStreaks();

        overlayManager.add(overlay);

        final BufferedImage icon = ImageUtil.loadImageResource(ThievingStreakPlugin.class, "thieving_icon.png"); // optional, or null

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

        m = HERB_START.matcher(message);
        if (m.matches())
        {
            startHerbHarvest();
            return;
        }

        m = HERB_EMPTY.matcher(message);
        if (m.matches())
        {
            endHerbHarvest();
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
            herbHarvestActive = false;
            herbItemId = -1;
            lastFarmingXpTick = -1;
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
        panel.updateCurrent(activeSkill, activeTarget, currentStreak);
    }

    private void handleFarmingDepleted()
    {
        if (activeSkill == SkillType.FARMING)
        {
            finishCurrentStreak();
        }
    }

    private void startHerbHarvest()
    {
        // If we’re already harvesting herbs on this patch, do nothing
        if (herbHarvestActive && activeSkill == SkillType.FARMING)
        {
            return;
        }
        
        finishCurrentStreak();

        activeSkill = SkillType.FARMING;
        activeTarget = null; // unknown until we see which herb appears
        currentStreak = 0;

        herbHarvestActive = true;
        herbItemId = -1;
        lastFarmingXpTick = -1;

        // we’ll detect herb type/name from inventory
        panel.updateCurrent(activeSkill, "Herb patch", 0);
    }

    private void endHerbHarvest()
    {
        if (!herbHarvestActive)
        {
            return;
        }

        herbHarvestActive = false;
        herbItemId = -1;
        lastFarmingXpTick = -1;
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
                    saveFarmingBestStreaks();
                }
                panel.updateFarmingBest(bestFarmingStreaks);
                break;
            }
        }

        activeSkill = null;
        activeTarget = null;
        currentStreak = 0;
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
            Map<String, Integer> map = GSON.fromJson(json, MAP_TYPE);
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
        String json = GSON.toJson(bestThievingStreaks);
        configManager.setConfiguration("thievingstreak", "bestStreaks", json);
    }

    private void saveFarmingBestStreaks()
    {
        String json = GSON.toJson(bestFarmingStreaks);
        configManager.setConfiguration("thievingstreak", "bestFarmingStreaks", json);
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

        if (herbHarvestActive
            && activeSkill == SkillType.FARMING
            && client.getTickCount() == lastFarmingXpTick)
        {
            // Detect and count herb gains
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

                // If we don't yet know which herb this patch is, try to identify it
                if (herbItemId == -1)
                {
                    String name = itemManager.getItemComposition(id).getName();
                    String lower = name.toLowerCase();

                    if (lower.contains("grimy"))
                    {
                        herbItemId = id;
                        activeTarget = name;
                    }
                }

                // Count only the chosen herb type
                if (id == herbItemId && herbItemId != -1)
                {
                    currentStreak += delta;
                    String label = (activeTarget != null && !activeTarget.isEmpty())
                            ? activeTarget
                            : "Herb patch";

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

        if (!herbHarvestActive)
        {
            return;
        }

        // Mark this tick as a "Farming XP tick" during an active herb harvest
        lastFarmingXpTick = client.getTickCount();
    }


}
