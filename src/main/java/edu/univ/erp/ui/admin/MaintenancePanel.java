package edu.univ.erp.ui.admin;

import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

class MaintenancePanel extends JPanel {
    private final edu.univ.erp.api.maintenance.MaintenanceAPI maintenanceAPI;
    private final JToggleButton toggleButton;
    private final JLabel statusLabel;

    MaintenancePanel(edu.univ.erp.api.maintenance.MaintenanceAPI maintenanceAPI) {
        this.maintenanceAPI = maintenanceAPI;
        setLayout(new MigLayout("fillx, wrap, insets 24", "[grow]", "[]"));
        setOpaque(false);
        setBackground(UIStyle.BACKGROUND_DARK);

        JPanel card = UIStyle.createCard();
        card.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]12[]16[]24[]"));

        JLabel title = UIStyle.createHeading("System Maintenance", 3);
        card.add(title, "growx, wrap");

        JLabel descLabel = UIStyle
                .createBodyLabel("Enable maintenance mode to temporarily disable user registrations and modifications");
        descLabel.setFont(UIStyle.FONT_BODY);
        card.add(descLabel, "growx, wrap");

        JPanel togglePanel = new JPanel(new MigLayout("fillx, insets 0", "[grow][]", "[]"));
        togglePanel.setOpaque(false);

        JLabel modeLabel = new JLabel("Maintenance Mode");
        modeLabel.setFont(UIStyle.FONT_BODY_BOLD);
        modeLabel.setForeground(UIStyle.TEXT_PRIMARY);
        togglePanel.add(modeLabel, "growx");

        toggleButton = new JToggleButton("OFF");
        toggleButton.setFont(UIStyle.FONT_BODY_BOLD);
        toggleButton.setPreferredSize(new Dimension(90, 42));
        toggleButton.setContentAreaFilled(false);
        toggleButton.setFocusPainted(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        updateToggleStyle();
        toggleButton.addItemListener(e -> updateToggleStyle());
        togglePanel.add(toggleButton, "growx 0");
        card.add(togglePanel, "growx, wrap");

        // Backup/Restore Section
        JPanel backupPanel = UIStyle.createCard();
        backupPanel.setLayout(new MigLayout("fillx, insets 20", "[grow]", "[]12[]"));

        JLabel backupTitle = UIStyle.createHeading("Data Management", 3);
        backupPanel.add(backupTitle, "wrap");

        JPanel backupButtonPanel = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
        backupButtonPanel.setOpaque(false);

        JButton backupButton = UIStyle.createSecondaryButton("Backup Data");
        backupButton.addActionListener(e -> backupData());
        backupButtonPanel.add(backupButton);

        JButton restoreButton = UIStyle.createSecondaryButton("Restore Data");
        restoreButton.addActionListener(e -> restoreData());
        backupButtonPanel.add(restoreButton);

        backupPanel.add(backupButtonPanel);
        card.add(backupPanel, "growx, wrap");

        JPanel buttonPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow][right][right]", "[]"));
        buttonPanel.setOpaque(false);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyle.FONT_BODY);
        statusLabel.setForeground(UIStyle.TEXT_SECONDARY);
        buttonPanel.add(statusLabel, "growx");

        JButton refreshButton = UIStyle.createSecondaryButton("🔄 Refresh");
        refreshButton.setPreferredSize(new Dimension(140, 44));
        refreshButton.addActionListener(e -> loadState());
        buttonPanel.add(refreshButton, "growx 0, gapright 12");

        JButton applyButton = UIStyle.createPrimaryButton("✓ Apply Changes");
        applyButton.setPreferredSize(new Dimension(170, 44));
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
            edu.univ.erp.api.common.ApiResponse<Boolean> response = maintenanceAPI.isMaintenanceMode();
            if (!response.isSuccess())
                throw new java.sql.SQLException(response.getMessage());
            boolean maintenance = response.getData();
            toggleButton.setSelected(maintenance);
            updateToggleStyle();
            statusLabel.setText(maintenance ? "⚠️ Maintenance mode is currently ENABLED"
                    : "✓ System is operational - Maintenance mode is OFF");
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
            edu.univ.erp.api.common.ApiResponse<Void> response = maintenanceAPI.setMaintenanceMode(maintenance);
            if (!response.isSuccess())
                throw new RuntimeException(response.getMessage());
            updateToggleStyle();
            statusLabel.setText(maintenance ? "⚠️ Maintenance mode is now ENABLED"
                    : "✓ Maintenance mode disabled - System is operational");
            statusLabel.setForeground(maintenance ? UIStyle.WARNING_ORANGE : UIStyle.SUCCESS_GREEN);
            JOptionPane.showMessageDialog(this, "Maintenance mode updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            statusLabel.setText("❌ Failed to update: " + ex.getMessage());
            statusLabel.setForeground(UIStyle.ERROR_RED);
            JOptionPane.showMessageDialog(this, "Failed to update maintenance mode: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void backupData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Backup File");
        fileChooser.setSelectedFile(new java.io.File("erp_backup_" + java.time.LocalDate.now() + ".sql"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                edu.univ.erp.service.BackupService backupService = new edu.univ.erp.service.BackupService();
                backupService.backupDatabase("ERPDB", file);
                JOptionPane.showMessageDialog(this, "Backup completed successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Backup failed: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void restoreData() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Restoring data will overwrite the current database. Are you sure?",
                "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION)
            return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Backup File");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                edu.univ.erp.service.BackupService backupService = new edu.univ.erp.service.BackupService();
                backupService.restoreDatabase("ERPDB", file);
                JOptionPane.showMessageDialog(this, "Restore completed successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Restore failed: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
