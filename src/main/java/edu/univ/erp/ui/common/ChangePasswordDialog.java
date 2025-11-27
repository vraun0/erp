package edu.univ.erp.ui.common;

import edu.univ.erp.service.AuthService;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class ChangePasswordDialog extends JDialog {
    private final AuthService authService;
    private final String username;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel errorLabel;

    public ChangePasswordDialog(Frame owner, AuthService authService, String username) {
        super(owner, "Change Password", true);
        this.authService = authService;
        this.username = username;

        initializeUI();
        initializeUI();
        setPreferredSize(new Dimension(450, 400));
        pack();
        setLocationRelativeTo(owner);
        setResizable(true);
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new MigLayout("fillx, wrap, insets 24", "[grow]", "[]16[]16[]16[]24[]"));
        mainPanel.setBackground(UIStyle.BACKGROUND_DARK);
        setContentPane(mainPanel);

        // Header
        JLabel titleLabel = UIStyle.createHeading("Change Password", 3);
        mainPanel.add(titleLabel, "growx");

        // Current Password
        mainPanel.add(createFieldPanel("Current Password", currentPasswordField = new JPasswordField()), "growx");

        // New Password
        mainPanel.add(createFieldPanel("New Password", newPasswordField = new JPasswordField()), "growx");

        // Confirm Password
        mainPanel.add(createFieldPanel("Confirm Password", confirmPasswordField = new JPasswordField()), "growx");

        // Error Label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(UIStyle.FONT_SMALL);
        errorLabel.setForeground(UIStyle.ERROR_RED);
        mainPanel.add(errorLabel, "growx");

        // Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "push[][]", "[]"));
        buttonPanel.setOpaque(false);

        JButton cancelButton = UIStyle.createSecondaryButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton, "width 100!");

        JButton saveButton = UIStyle.createPrimaryButton("Save Changes");
        saveButton.addActionListener(e -> changePassword());
        buttonPanel.add(saveButton, "width 140!");

        mainPanel.add(buttonPanel, "growx");
    }

    private JPanel createFieldPanel(String labelText, JComponent field) {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow]", "[]4[]"));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(UIStyle.FONT_BODY_BOLD);
        label.setForeground(UIStyle.TEXT_SECONDARY);
        panel.add(label, "wrap");

        field.setFont(UIStyle.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIStyle.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        panel.add(field, "growx");

        return panel;
    }

    private void changePassword() {
        String currentPass = new String(currentPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            errorLabel.setText("New passwords do not match.");
            return;
        }

        if (newPass.length() < 6) {
            errorLabel.setText("New password must be at least 6 characters.");
            return;
        }

        try {
            authService.changePassword(username, currentPass, newPass);
            JOptionPane.showMessageDialog(this, "Password changed successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (ServiceException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (SQLException ex) {
            errorLabel.setText("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
