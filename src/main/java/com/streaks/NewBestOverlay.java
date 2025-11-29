package com.streaks;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import java.awt.*;

public class NewBestOverlay extends Overlay
{
    private static final long DURATION_MS = 5000L;

    private final StreaksPlugin plugin;

    @Inject
    public NewBestOverlay(StreaksPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.DETACHED);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    @Override
    public Dimension render(Graphics2D g)
    {
        if (!plugin.isCelebrationActive())
        {
            return null;
        }

        long start = plugin.getCelebrateStartMillis();
        long now = System.currentTimeMillis();
        long elapsed = now - start;

        if (elapsed >= DURATION_MS)
        {
            plugin.clearCelebration();
            return null;
        }

        double t = elapsed / (double) DURATION_MS;
        float alpha = (float) (1.0 - t); // fade out over time
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));

        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.SrcOver.derive(alpha));

        String skillName = StringUtils.capitalize(plugin.getCelebrateSkill().name().toLowerCase());
        String target = plugin.getCelebrateTarget();
        int value = plugin.getCelebrateValue();

        String line1 = "NEW BEST!";
        String line2 = skillName + " - " + target;
        String line3 = "Streak: " + value;

        Font baseFont = g.getFont();
        Font big = baseFont.deriveFont(Font.BOLD, 24f);
        Font mid = baseFont.deriveFont(Font.BOLD, 16f);
        Font small = baseFont.deriveFont(Font.PLAIN, 14f);

        FontMetrics fmBig = g.getFontMetrics(big);
        FontMetrics fmMid = g.getFontMetrics(mid);
        FontMetrics fmSmall = g.getFontMetrics(small);

        int width1 = fmBig.stringWidth(line1);
        int width2 = fmMid.stringWidth(line2);
        int width3 = fmSmall.stringWidth(line3);
        int maxWidth = Math.max(width1, Math.max(width2, width3)) + 40;

        int lineHeight = fmBig.getHeight() + fmMid.getHeight() + fmSmall.getHeight() + 30;

        // Center on screen
        int width = plugin.getClient().getCanvas().getWidth();
        int height = plugin.getClient().getCanvas().getHeight();

        int x = (width - maxWidth) / 2;
        int y = height / 5;

        // Background
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRoundRect(x, y, maxWidth, lineHeight, 20, 20);

        // Border
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(255, 215, 0, 220));
        g.drawRoundRect(x, y, maxWidth, lineHeight, 20, 20);

        int cx = x + maxWidth / 2;
        int ty = y + 10;

        // Draw text
        g.setColor(Color.WHITE);

        g.setFont(big);
        g.drawString(line1, cx - width1 / 2, ty + fmBig.getAscent());
        ty += fmBig.getHeight();

        g.setFont(mid);
        g.drawString(line2, cx - width2 / 2, ty + fmMid.getAscent());
        ty += fmMid.getHeight();

        g.setFont(small);
        g.drawString(line3, cx - width3 / 2, ty + fmSmall.getAscent());

        g.setComposite(old);
        return null;
    }
}
