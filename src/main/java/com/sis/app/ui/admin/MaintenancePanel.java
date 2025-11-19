package com.sis.app.ui.admin;

import com.sis.app.service.AdminService;
import com.sis.app.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

class MaintenancePanel extends JPanel {
    private final AdminService adminService;
    private final JToggleButton toggleButton;
    private final JLabel statusLabel;

    MaintenancePanel(AdminService adminService) {
        this.adminService = adminService;
        setLayout(new MigLayout("fillx, wrap, insets 20", "[grow]", "[]"));
        setOpaque(false);
        setBackground(UIStyle.BACKGROUND_DARK);
        
        JPanel card = UIStyle.createCard();
        card.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]8[]12[]15[]"));
        
        JLabel title = UIStyle.createHeading("System Maintenance", 3);
        card.add(title, "growx, wrap");
        
        JLabel descLabel = UIStyle.createBodyLabel("Enable maintenance mode to temporarily disable user registrations and modifications");
        card.add(descLabel, "growx, wrap");
        
        JPanel togglePanel = new JPanel(new MigLayout("fillx, insets 0", "[grow][]", "[]"));
        togglePanel.setOpaque(false);
        
        JLabel modeLabel = new JLabel("Maintenance Mode");
        modeLabel.setFont(UIStyle.FONT_BODY_BOLD);
        modeLabel.setForeground(UIStyle.TEXT_PRIMARY);
        togglePanel.add(modeLabel, "growx");
        
        toggleButton = new JToggleButton("OFF");
        toggleButton.setFont(UIStyle.FONT_BODY_BOLD);
        toggleButton.setPreferredSize(new Dimension(80, 38));
        toggleButton.setContentAreaFilled(false);
        toggleButton.setFocusPainted(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        updateToggleStyle();
        toggleButton.addItemListener(e -> updateToggleStyle());
        togglePanel.add(toggleButton, "growx 0");
        card.add(togglePanel, "growx, wrap");
        
        JPanel buttonPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow][right][right]", "[]"));
        buttonPanel.setOpaque(false);
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyle.FONT_SMALL);
        statusLabel.setForeground(UIStyle.TEXT_SECONDARY);
        buttonPanel.add(statusLabel, "growx");
        
        JButton refreshButton = UIStyle.createSecondaryButton("🔄 Refresh");
        refreshButton.setPreferredSize(new Dimension(120, 40));
        refreshButton.addActionListener(e -> loadState());
        buttonPanel.add(refreshButton, "growx 0");
        
        JButton applyButton = UIStyle.createPrimaryButton("✓ Apply Changes");
        applyButton.setPreferredSize(new Dimension(150, 40));
        applyButton.addActionListener(e -> applyChange());
        buttonPanel.add(applyButton, "growx 0");
        
        card.add(buttonPanel, "growx");
        add(card, "growx");
        
        loadState();
    }
    
    private void updateToggleStyle() {
        boolean isOn = toggleButton.isSelected();
        toggleButton.setText(isOn ? "ON" : "OFF");
        toggleButton.setBackground(isOn ? UIStyle.ERROR_RED : UIStyle.SUCCESS_GREEN);
        toggleButton.setForeground(Color.WHITE);
        toggleButton.setOpaque(true);
        toggleButton.repaint();
    }

    void loadState() {
        try {
            boolean maintenance = adminService.isMaintenanceMode();
            toggleButton.setSelected(maintenance);
            updateToggleStyle();
            statusLabel.setText(maintenance ? "⚠️ Maintenance mode is currently ENABLED" : 
                "✓ System is operational - Maintenance mode is OFF");
            statusLabel.setForeground(maintenance ? UIStyle.WARNING_ORANGE : UIStyle.SUCCESS_GREEN);
        } catch (SQLException ex) {
            statusLabel.setText("❌ Failed to load maintenance state: " + ex.getMessage());
            statusLabel.setForeground(UIStyle.ERROR_RED);
            JOptionPane.showMessageDialog(this, "Failed to load maintenance state: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyChange() {
        try {
            boolean maintenance = toggleButton.isSelected();
            adminService.setMaintenanceMode(maintenance);
            updateToggleStyle();
            statusLabel.setText(maintenance ? "⚠️ Maintenance mode is now ENABLED" : 
                "✓ Maintenance mode disabled - System is operational");
            statusLabel.setForeground(maintenance ? UIStyle.WARNING_ORANGE : UIStyle.SUCCESS_GREEN);
            JOptionPane.showMessageDialog(this, "Maintenance mode updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            statusLabel.setText("❌ Failed to update: " + ex.getMessage());
            statusLabel.setForeground(UIStyle.ERROR_RED);
            JOptionPane.showMessageDialog(this, "Failed to update maintenance mode: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

