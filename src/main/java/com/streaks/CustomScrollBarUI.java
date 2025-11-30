package com.streaks;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class CustomScrollBarUI extends BasicScrollBarUI
{
    @Override
    protected void configureScrollBarColors()
    {
        this.thumbColor = new Color(180, 180, 180);
        this.trackColor = new Color(50, 50, 50);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds)
    {
        if (!c.isEnabled()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(thumbColor);

        int arc = thumbBounds.width;
        g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, arc, arc);

        g2.dispose();
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds)
    {
        // Do nothing, make track invisible for a cleaner look
    }

    @Override
    protected JButton createDecreaseButton(int orientation)
    {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation)
    {
        return createZeroButton();
    }

    private JButton createZeroButton()
    {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(0, 0));
        btn.setMinimumSize(new Dimension(0, 0));
        btn.setMaximumSize(new Dimension(0, 0));
        return btn;
    }
}
