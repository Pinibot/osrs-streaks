package com.streaks;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

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

        String title = "Streak Tracker";
        String skillName = StringUtils.capitalize(skill.name().toLowerCase());
        String mainLeftText = skillName + " - " + target;
        String mainRightText = Integer.toString(streak);

        int best = plugin.getBestStreakFor(skill, target);
        String bestLeft  = "Best streak:";
        String bestRight = Integer.toString(best);

        double secondsRemaining = plugin.getSecondsRemainingInStreak();
        String timeLeftText = "Time until streak is over: ";
        String timeRightText = String.format("%.1fs", secondsRemaining);

        panelComponent.getChildren().add(
            TitleComponent.builder()
                .text(title)
                .build()
        );

        panelComponent.getChildren().add(
            LineComponent.builder()
                .left(mainLeftText)
                .right(mainRightText)
                .build()
        );

        panelComponent.getChildren().add(
            LineComponent.builder()
                .left(bestLeft)
                .right(bestRight)
                .build()
        );

        panelComponent.getChildren().add(
            LineComponent.builder()
                .left(timeLeftText)
                .right(timeRightText)
                .build()
        );

        // Set dynamic width
        FontMetrics fm = graphics.getFontMetrics();
        int maxWidth = 0;
        maxWidth = Math.max(maxWidth, fm.stringWidth(title));
        maxWidth = Math.max(maxWidth, fm.stringWidth(mainLeftText + " " + mainRightText));
        if (!timeRightText.isEmpty())
        {
            maxWidth = Math.max(maxWidth, fm.stringWidth(timeLeftText + " " + timeRightText));
        }

        maxWidth += 20; // padding

        panelComponent.setPreferredSize(new Dimension(maxWidth, 0));

        return super.render(graphics);
    }
}
