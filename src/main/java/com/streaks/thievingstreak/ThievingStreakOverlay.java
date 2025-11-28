package com.streaks.thievingstreak;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class ThievingStreakOverlay extends OverlayPanel
{
    private final ThievingStreakPlugin plugin;

    @Inject
    private ThievingStreakOverlay(ThievingStreakPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        String npc = plugin.getActiveNpc();
        int streak = plugin.getCurrentStreak();

        if (npc == null || streak <= 0)
        {
            return null;
        }

        panelComponent.getChildren().add(
                TitleComponent.builder()
                        .text("Pickpocket streak")
                        .build()
        );

        panelComponent.getChildren().add(
                LineComponent.builder()
                        .left(npc)
                        .right(Integer.toString(streak))
                        .build()
        );

        return super.render(graphics);
    }
}
