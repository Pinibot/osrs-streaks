package com.streaks;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.Random;

public class CelebrationOverlay extends Overlay
{
    private static final long DURATION_MS = 5000L;
    private static final int CONFETTI_COUNT = 800;

    private final StreaksPlugin plugin;
    private final Client client;

    @Inject
    public CelebrationOverlay(StreaksPlugin plugin, Client client)
    {
        this.plugin = plugin;
        this.client = client;

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
        float alpha = (float) (1.0 - t);
        alpha = alpha > 0.5f ? 1.0f : alpha;
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));

        Composite oldComposite = g.getComposite();
        g.setComposite(AlphaComposite.SrcOver.derive(alpha));

        int canvasWidth = client.getCanvas().getWidth();
        int canvasHeight = client.getCanvas().getHeight();

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

        int boxHeight = fmBig.getHeight() + fmMid.getHeight() + fmSmall.getHeight() + 30;

        int x = (canvasWidth - maxWidth) / 2;
        int y = canvasHeight / 5;
        
        int cx = x + maxWidth / 2;
        int ty = y + 10;

        // Confetti burst around the popup
        drawConfetti(g, canvasWidth, canvasHeight, cx, y + boxHeight / 2, elapsed);

        // Background box
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRoundRect(x, y, maxWidth, boxHeight, 20, 20);

        // Gold border
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(255, 215, 0, 230));
        g.drawRoundRect(x, y, maxWidth, boxHeight, 20, 20);

        // Text
        g.setColor(Color.WHITE);

        g.setFont(big);
        g.drawString(line1, cx - width1 / 2, ty + fmBig.getAscent());
        ty += fmBig.getHeight();

        g.setFont(mid);
        g.drawString(line2, cx - width2 / 2, ty + fmMid.getAscent());
        ty += fmMid.getHeight();

        g.setFont(small);
        g.drawString(line3, cx - width3 / 2, ty + fmSmall.getAscent());

        g.setComposite(oldComposite);
        return null;
    }

    private void drawConfetti(Graphics2D g, int canvasWidth, int canvasHeight, int centerX, int centerY, long elapsed)
    {
        double raw = elapsed / (double) DURATION_MS;
        raw = Math.max(0.0, Math.min(1.0, raw));
        double t = 1 - Math.pow(1 - raw, 5);

        Color[] colors = new Color[]{
                new Color(255, 99, 132),
                new Color(54, 162, 235),
                new Color(255, 206, 86),
                new Color(75, 192, 192),
                new Color(153, 102, 255),
                new Color(255, 159, 64)
        };

        long baseSeed = plugin.getCelebrateStartMillis();

        for (int i = 0; i < CONFETTI_COUNT; i++)
        {
            Random r = new Random(baseSeed + i * 31L);

            double angle = r.nextDouble() * 2.0 * Math.PI;        // full 360°
            double maxRadius = 240.0;
            double radius = (20 + r.nextDouble() * (maxRadius - 20)) * t;

            double px = centerX + Math.cos(angle) * radius * t;
            double py = centerY + Math.sin(angle) * radius * t;

            int size = 3 + r.nextInt(4);
            Color c = colors[r.nextInt(colors.length)];

            int drawX = (int) px;
            int drawY = (int) py;

            if (drawX < 0 || drawX > canvasWidth || drawY < 0 || drawY > canvasHeight)
            {
                continue;
            }

            g.setColor(c);
            g.fillRect(drawX, drawY, size, size);
        }
    }
}
