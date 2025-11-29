package com.streaks;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class StreaksOverlay extends OverlayPanel
{
    private final StreaksPlugin plugin;

    @Inject
    private StreaksOverlay(StreaksPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        StreaksPlugin.SkillType skill = plugin.getActiveSkill();
        String target = plugin.getActiveTarget();
        int streak = plugin.getCurrentStreak();

        if (skill == null || target == null || streak <= 0)
        {
            return null;
        }

        String title = "Streak";
        String left = (skill == StreaksPlugin.SkillType.THIEVING ? "Thieving - " : "Farming - ") + target;

        panelComponent.getChildren().add(
                TitleComponent.builder()
                        .text(title)
                        .build()
        );

        panelComponent.getChildren().add(
                LineComponent.builder()
                        .left(left)
                        .right(Integer.toString(streak))
                        .build()
        );

        return super.render(graphics);
    }
}
