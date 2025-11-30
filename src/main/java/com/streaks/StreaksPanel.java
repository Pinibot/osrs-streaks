package com.streaks;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;

import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.Map;

public class StreaksPanel extends PluginPanel
{
    private final StreaksPlugin plugin;

    private final JLabel currentTargetValue = new JLabel("---");
    private final JLabel currentStreakValue = new JLabel("---");

    private final JPanel thievingContainer = new JPanel();
    private final JPanel farmingContainer = new JPanel();

    @Inject
    public StreaksPanel(StreaksPlugin plugin)
    {
        this.plugin = plugin;

        getScrollPane().setBorder(null);

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // TOP: current streak info
        JPanel statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 4, 0);

        JLabel title = createTitleLabel("Streak Tracker");
        statsPanel.add(title, c);

        c.gridy++;
        c.insets = new Insets(0, 0, 8, 0);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        statsPanel.add(createSeparator(), c);

        c.gridy++;
        c.insets = new Insets(0, 0, 2, 0);
        statsPanel.add(createStatRow("Current:", currentTargetValue), c);

        c.gridy++;
        statsPanel.add(createStatRow("Current streak:", currentStreakValue), c);

        add(statsPanel, BorderLayout.NORTH);

        // CENTER: collapsible skill sections
        JPanel sections = new JPanel();
        sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));
        sections.setOpaque(false);

        sections.add(createSkillSection("Thieving", StreaksPlugin.SkillType.THIEVING, thievingContainer));
        sections.add(Box.createVerticalStrut(8));
        sections.add(createSkillSection("Farming", StreaksPlugin.SkillType.FARMING, farmingContainer));

        add(sections, BorderLayout.CENTER);

        // BOTTOM: reset-all button
        JButton resetAllButton = createResetAllButton();

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        bottom.add(resetAllButton);

        add(bottom, BorderLayout.SOUTH);
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

    private JPanel createSkillSection(String title,
                                      StreaksPlugin.SkillType skill,
                                      JPanel contentPanel)
    {
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JButton headerButton = new JButton();
        headerButton.setFocusable(false);
        headerButton.setContentAreaFilled(false);
        headerButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        headerButton.setHorizontalAlignment(SwingConstants.LEFT);
        headerButton.setForeground(Color.WHITE);
        headerButton.setFont(headerButton.getFont().deriveFont(Font.BOLD));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.setPreferredSize(new Dimension(0, 120));
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.getVerticalScrollBar().setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        setSectionExpanded(headerButton, scrollPane, title, true);

        headerButton.addActionListener(e ->
        {
            boolean expanded = !scrollPane.isVisible();
            setSectionExpanded(headerButton, scrollPane, title, expanded);
            revalidate();
        });

        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.add(headerButton, BorderLayout.NORTH);
        section.add(scrollPane, BorderLayout.CENTER);

        return section;
    }

    private void setSectionExpanded(JButton headerButton, JScrollPane content, String title, boolean expanded)
    {
        content.setVisible(expanded);
        headerButton.setText((expanded ? "▼ " : "► ") + title);
    }

    private JPanel createBestRow(StreaksPlugin.SkillType skill, String key, int streak)
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

        JLabel keyLabel = new JLabel(key + ":");
        keyLabel.setForeground(Color.LIGHT_GRAY);

        JLabel streakLabel = new JLabel(Integer.toString(streak));
        streakLabel.setForeground(Color.WHITE);
        streakLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(keyLabel, BorderLayout.WEST);
        textPanel.add(streakLabel, BorderLayout.EAST);

        JButton deleteButton = new JButton("🗑");
        deleteButton.setMargin(new Insets(0, 4, 0, 4));
        deleteButton.setFocusable(false);
        deleteButton.setBorder(BorderFactory.createEmptyBorder());
        deleteButton.setContentAreaFilled(false);
        deleteButton.setOpaque(false);
        deleteButton.setToolTipText("Delete streak for " + key);

        Color normal = Color.LIGHT_GRAY;
        Color hover = Color.WHITE;
        deleteButton.setForeground(normal);

        deleteButton.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e)
            {
                deleteButton.setForeground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e)
            {
                deleteButton.setForeground(normal);
            }
        });

        deleteButton.addActionListener(e ->
        {
            int res = JOptionPane.showConfirmDialog(
                    this,
                    "Delete best streak for \"" + key + "\" (" +
                            StringUtils.capitalize(skill.name().toLowerCase()) + ")?",
                    "Confirm delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (res == JOptionPane.YES_OPTION)
            {
                plugin.deleteStreak(skill, key);
            }
        });

        row.add(textPanel, BorderLayout.CENTER);
        row.add(deleteButton, BorderLayout.EAST);

        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

        return row;
    }

    private JButton createResetAllButton()
    {
        JButton resetAllButton = new JButton("Reset all");

        resetAllButton.setFocusable(false);
        resetAllButton.setToolTipText("Reset all saved best streaks");

        resetAllButton.setForeground(Color.WHITE);
        resetAllButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        resetAllButton.setOpaque(true);
        resetAllButton.setContentAreaFilled(true);
        resetAllButton.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));
        resetAllButton.setFocusPainted(false);

        resetAllButton.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e)
            {
                resetAllButton.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e)
            {
                resetAllButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            }
        });

        resetAllButton.addActionListener(e ->
        {
            int res = JOptionPane.showConfirmDialog(
                    this,
                    "Reset ALL saved best streaks for all skills?\nThis cannot be undone.",
                    "Confirm reset",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (res == JOptionPane.YES_OPTION)
            {
                plugin.resetAllStreaks();
            }
        });

        return resetAllButton;
    }

    // Called by plugin

    public void updateCurrent(StreaksPlugin.SkillType skill, String target, int streak)
    {
        if (skill == null || target == null || target.isEmpty() || streak <= 0)
        {
            currentTargetValue.setText("---");
            currentStreakValue.setText("---");
        }
        else
        {
            String skillName = StringUtils.capitalize(skill.name().toLowerCase());
            currentTargetValue.setText(skillName + " - " + target);
            currentStreakValue.setText(String.valueOf(streak));
        }
    }

    public void updateThievingBest(Map<String, Integer> best)
    {
        updateSkillContainer(StreaksPlugin.SkillType.THIEVING, thievingContainer, best);
    }

    public void updateFarmingBest(Map<String, Integer> best)
    {
        updateSkillContainer(StreaksPlugin.SkillType.FARMING, farmingContainer, best);
    }

    private void updateSkillContainer(StreaksPlugin.SkillType skill,
                                      JPanel container,
                                      Map<String, Integer> best)
    {
        container.removeAll();

        if (best == null || best.isEmpty())
        {
            JLabel emptyLabel = new JLabel("No data yet");
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.add(emptyLabel, BorderLayout.CENTER);

            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));
            container.add(wrapper);
        }
        else
        {
            best.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .forEach(e ->
                    {
                        JPanel row = createBestRow(skill, e.getKey(), e.getValue());
                        container.add(row);
                    });
        }

        container.revalidate();
        container.repaint();
    }
}
