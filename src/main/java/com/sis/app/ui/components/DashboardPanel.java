package com.sis.app.ui.components;

import com.sis.app.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Base dashboard panel with card-based layout
 * Provides consistent styling and layout for ERP modules
 */
public class DashboardPanel extends JPanel {
    protected final JPanel contentPanel;
    
    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_DARK);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        
        // Scrollable content panel
        contentPanel = new JPanel();
        contentPanel.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]16[]"));
        contentPanel.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        UIStyle.styleScrollPane(scrollPane);
        scrollPane.getViewport().setBackground(UIStyle.BACKGROUND_DARK);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Add a section header
     */
    protected void addSectionHeader(String title, String subtitle) {
        JPanel header = new JPanel(new MigLayout("fillx, insets 0", "[grow][]", "[]4[]"));
        header.setOpaque(false);
        
        JLabel titleLabel = UIStyle.createHeading(title, 2);
        header.add(titleLabel, "growx, wrap");
        
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel subtitleLabel = UIStyle.createBodyLabel(subtitle);
            header.add(subtitleLabel, "growx");
        }
        
        contentPanel.add(header, "growx, wrap, gapbottom 8");
    }
    
    /**
     * Add a metric card
     */
    protected JPanel addMetricCard(String title, String value, String icon, Color accentColor) {
        JPanel card = UIStyle.createCard();
        card.setLayout(new MigLayout("fillx, insets 20", "[grow]", "[]8[]"));
        card.setPreferredSize(new Dimension(280, 140));
        
        // Icon and title row
        JPanel header = new JPanel(new MigLayout("fillx, insets 0", "[][]push", "[]"));
        header.setOpaque(false);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font(UIStyle.FONT_FAMILY, Font.PLAIN, 24));
        header.add(iconLabel, "growx 0, gapright 12");
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIStyle.FONT_SMALL);
        titleLabel.setForeground(UIStyle.TEXT_SECONDARY);
        header.add(titleLabel, "growx");
        
        card.add(header, "growx, wrap");
        
        // Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(UIStyle.FONT_H2);
        valueLabel.setForeground(accentColor);
        card.add(valueLabel, "growx");
        
        contentPanel.add(card, "growx");
        
        return card;
    }
    
    /**
     * Add a content card
     */
    protected JPanel addContentCard(String title, JComponent content) {
        JPanel card = UIStyle.createCard();
        card.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]16[]"));
        
        if (title != null) {
            JPanel header = new JPanel(new MigLayout("fillx, insets 0 0 16 0", "[grow]", "[]"));
            header.setOpaque(false);
            JLabel titleLabel = UIStyle.createHeading(title, 4);
            header.add(titleLabel, "growx");
            card.add(header, "growx, wrap");
        }
        
        card.add(content, "growx");
        contentPanel.add(card, "growx, wrap");
        
        return card;
    }
    
    /**
     * Add a grid of cards
     */
    protected void addCardGrid(JComponent... cards) {
        JPanel grid = new JPanel(new MigLayout("fillx, wrap, insets 0", 
            "[grow 0][grow 0][grow 0]", "[]"));
        grid.setOpaque(false);
        
        for (JComponent card : cards) {
            grid.add(card, "growx");
        }
        
        contentPanel.add(grid, "growx, wrap");
    }
    
    /**
     * Clear all content
     */
    public void clearContent() {
        contentPanel.removeAll();
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}

