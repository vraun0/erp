package com.sis.app.ui.components;

import com.sis.app.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modern collapsible sidebar navigation panel
 */
public class SidebarPanel extends JPanel {
    private boolean isCollapsed = false;
    private final int expandedWidth = UIStyle.SIDEBAR_WIDTH;
    private final int collapsedWidth = UIStyle.SIDEBAR_COLLAPSED_WIDTH;
    
    private final JPanel menuPanel;
    private final List<SidebarItem> menuItems = new ArrayList<>();
    private final Map<String, JButton> itemButtons = new HashMap<>();
    private String activeItem = null;
    
    public SidebarPanel() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.SIDEBAR_BG);
        setPreferredSize(new Dimension(expandedWidth, 0));
        
        // Header with logo/title
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);
        
        // Menu items panel
        menuPanel = new JPanel(new MigLayout("fillx, wrap, insets 8 12 8 12", "[fill]", "[]4[]"));
        menuPanel.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UIStyle.SIDEBAR_BG);
        scrollPane.setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        UIStyle.styleScrollPane(scrollPane);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Collapse button at bottom
        JPanel footerPanel = createFooter();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new MigLayout("fillx, insets 16", "[grow]", "[]"));
        header.setOpaque(false);
        
        JLabel logoLabel = new JLabel("📊");
        logoLabel.setFont(new Font(UIStyle.FONT_FAMILY, Font.PLAIN, 28));
        header.add(logoLabel, "growx 0, gapright 12");
        
        JLabel titleLabel = new JLabel("ERP System");
        titleLabel.setFont(UIStyle.FONT_H4);
        titleLabel.setForeground(UIStyle.TEXT_PRIMARY);
        header.add(titleLabel, "growx");
        
        return header;
    }
    
    private JPanel createFooter() {
        JPanel footer = new JPanel(new MigLayout("fillx, insets 8 12 12 12", "[fill]", "[]"));
        footer.setOpaque(false);
        
        JButton collapseBtn = createCollapseButton();
        footer.add(collapseBtn, "growx");
        
        return footer;
    }
    
    private JButton createCollapseButton() {
        JButton btn = new JButton("☰");
        btn.setFont(new Font(UIStyle.FONT_FAMILY, Font.PLAIN, 18));
        btn.setForeground(UIStyle.SIDEBAR_TEXT);
        btn.setBackground(UIStyle.SIDEBAR_BG);
        btn.setBorder(new EmptyBorder(10, 10, 10, 10));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> toggleCollapse());
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(UIStyle.SIDEBAR_HOVER);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(UIStyle.SIDEBAR_BG);
            }
        });
        
        return btn;
    }
    
    public void addMenuItem(String id, String icon, String label, ActionListener listener) {
        SidebarItem item = new SidebarItem(id, icon, label, listener);
        menuItems.add(item);
        
        JButton menuBtn = createMenuButton(item);
        itemButtons.put(id, menuBtn);
        menuPanel.add(menuBtn, "growx, h 48!");
    }
    
    private JButton createMenuButton(SidebarItem item) {
        JButton btn = new JButton(item.label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (item.id.equals(activeItem)) {
                    g2.setColor(UIStyle.SIDEBAR_ACTIVE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(UIStyle.SIDEBAR_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        btn.setFont(UIStyle.FONT_BODY);
        btn.setForeground(item.id.equals(activeItem) ? Color.WHITE : UIStyle.SIDEBAR_TEXT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 16, 12, 16));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);
        
        // Icon + Text layout
        btn.setText("<html><span style='font-size:18px;'>" + item.icon + 
                   "</span> <span style='margin-left:12px;'>" + item.label + "</span></html>");
        
        btn.addActionListener(e -> {
            setActiveItem(item.id);
            if (item.listener != null) {
                item.listener.actionPerformed(e);
            }
        });
        
        return btn;
    }
    
    public void setActiveItem(String id) {
        if (activeItem != null && itemButtons.containsKey(activeItem)) {
            JButton prevBtn = itemButtons.get(activeItem);
            prevBtn.setForeground(UIStyle.SIDEBAR_TEXT);
            prevBtn.repaint();
        }
        
        activeItem = id;
        if (itemButtons.containsKey(id)) {
            JButton activeBtn = itemButtons.get(id);
            activeBtn.setForeground(Color.WHITE);
            activeBtn.repaint();
        }
    }
    
    private void toggleCollapse() {
        isCollapsed = !isCollapsed;
        int newWidth = isCollapsed ? collapsedWidth : expandedWidth;
        setPreferredSize(new Dimension(newWidth, 0));
        
        // Update all menu buttons to show/hide text
        for (SidebarItem item : menuItems) {
            JButton btn = itemButtons.get(item.id);
            if (isCollapsed) {
                btn.setText("<html><span style='font-size:18px;'>" + item.icon + "</span></html>");
            } else {
                btn.setText("<html><span style='font-size:18px;'>" + item.icon + 
                           "</span> <span style='margin-left:12px;'>" + item.label + "</span></html>");
            }
        }
        
        revalidate();
        repaint();
        
        // Notify parent to update layout
        Container parent = getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }
    
    public boolean isCollapsed() {
        return isCollapsed;
    }
    
    private static class SidebarItem {
        final String id;
        final String icon;
        final String label;
        final ActionListener listener;
        
        SidebarItem(String id, String icon, String label, ActionListener listener) {
            this.id = id;
            this.icon = icon;
            this.label = label;
            this.listener = listener;
        }
    }
}

