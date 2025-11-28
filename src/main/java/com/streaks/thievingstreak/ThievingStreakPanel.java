package com.streaks.thievingstreak;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.util.Map;

public class ThievingStreakPanel extends PluginPanel
{
    private final JLabel currentNpcValue = new JLabel("---");
    private final JLabel currentStreakValue = new JLabel("---");

    private final JPanel bestStreaksContainer = new JPanel();

    @Inject
    public ThievingStreakPanel()
    {
        getScrollPane().setBorder(null);

        JPanel container = getWrappedPanel();
        container.setLayout(new BorderLayout());
        container.setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === TOP: current session stats ===
        JPanel statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 4, 0);

        // Title
        statsPanel.add(createTitleLabel("Thieving Streaks"), c);
        c.gridy++;

        // Separator
        c.insets = new Insets(0, 0, 8, 0);
        statsPanel.add(createSeparator(), c);
        c.gridy++;

        // Current NPC row
        c.insets = new Insets(0, 0, 2, 0);
        statsPanel.add(createStatRow("Current NPC:", currentNpcValue), c);
        c.gridy++;

        // Current streak row
        statsPanel.add(createStatRow("Current streak:", currentStreakValue), c);
        c.gridy++;

        // Spacing
        c.weighty = 1;
        statsPanel.add(Box.createVerticalStrut(8), c);

        container.add(statsPanel, BorderLayout.NORTH);

        // === CENTER: best streaks list ===
        bestStreaksContainer.setLayout(new BoxLayout(bestStreaksContainer, BoxLayout.Y_AXIS));
        bestStreaksContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(bestStreaksContainer);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Best streaks"));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);

        scrollPane.setPreferredSize(new Dimension(0, 200));

        container.add(scrollPane, BorderLayout.CENTER);
    }

    private JLabel createTitleLabel(String text)
    {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        return label;
    }

    private JSeparator createSeparator()
    {
        JSeparator separator = new JSeparator();
        separator.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        return separator;
    }

    private JPanel createStatRow(String labelText, JLabel valueLabel)
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setForeground(Color.LIGHT_GRAY);

        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(label, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);

        return row;
    }

    private JPanel createBestRow(String npc, int streak)
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

        JLabel npcLabel = new JLabel(npc + ":");
        npcLabel.setForeground(Color.LIGHT_GRAY);

        JLabel streakLabel = new JLabel(Integer.toString(streak));
        streakLabel.setForeground(Color.WHITE);
        streakLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(npcLabel, BorderLayout.WEST);
        row.add(streakLabel, BorderLayout.EAST);

        // Make the row stretch horizontally in BoxLayout
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

        return row;
    }

    // === Called by plugin ===

    public void updateCurrent(String npc, int streak)
    {
        if (npc == null || npc.isEmpty() || streak <= 0)
        {
            currentNpcValue.setText("---");
            currentStreakValue.setText("---");
        }
        else
        {
            currentNpcValue.setText(npc);
            currentStreakValue.setText(String.valueOf(streak));
        }
    }

    public void updateBestStreaks(Map<String, Integer> bestStreaks)
    {
        bestStreaksContainer.removeAll();

        if (bestStreaks == null || bestStreaks.isEmpty())
        {
            JLabel emptyLabel = new JLabel("No data yet");
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.add(emptyLabel, BorderLayout.CENTER);

            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));
            bestStreaksContainer.add(wrapper);
        }
        else
        {
            bestStreaks.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .forEach(e ->
                    {
                        JPanel row = createBestRow(e.getKey(), e.getValue());
                        bestStreaksContainer.add(row);
                    });
        }

        bestStreaksContainer.revalidate();
        bestStreaksContainer.repaint();
    }
}
