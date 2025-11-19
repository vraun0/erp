package com.sis.app.ui;

import com.sis.app.model.User;
import com.sis.app.service.AuthService;
import com.sis.app.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;

/**
 * Modern card-based login window with professional ERP design.
 */
public class LoginFrame extends JFrame {
    private final AuthService authService;
    private final MainFrame mainFrame;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginButton;

    public LoginFrame(AuthService authService, MainFrame mainFrame) {
        super("University ERP System - Login");
        this.authService = authService;
        this.mainFrame = mainFrame;

        initializeComponents();
        configureLayout();
        registerListeners();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 680);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(480, 600));
        getContentPane().setBackground(UIStyle.BACKGROUND_DARK);
    }

    private void initializeComponents() {
        usernameField = UIStyle.createTextField();
        usernameField.putClientProperty("JTextField.placeholderText", "Enter your username");
        
        passwordField = UIStyle.createPasswordField();
        passwordField.putClientProperty("JTextField.placeholderText", "Enter your password");
        
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(UIStyle.FONT_SMALL);
        statusLabel.setForeground(UIStyle.ERROR_RED);
        
        loginButton = UIStyle.createPrimaryButton("Sign In");
        loginButton.setPreferredSize(new Dimension(0, 50));
    }

    private void configureLayout() {
        setLayout(new BorderLayout());
        
        // Main container with padding
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBorder(new EmptyBorder(40, 40, 40, 40));
        mainContainer.setBackground(UIStyle.BACKGROUND_DARK);
        
        // Header section
        JPanel headerPanel = new JPanel(new MigLayout("fillx, wrap, insets 0", "[center]", "[]12[]8[]"));
        headerPanel.setOpaque(false);
        
        // University logo placeholder (can be replaced with actual logo)
        JLabel logoLabel = new JLabel("🎓", SwingConstants.CENTER);
        logoLabel.setFont(new Font(UIStyle.FONT_FAMILY, Font.PLAIN, 48));
        headerPanel.add(logoLabel, "growx, wrap");
        
        JLabel titleLabel = UIStyle.createHeading("University ERP System", 1);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, "growx, wrap");
        
        JLabel subtitleLabel = UIStyle.createBodyLabel("Student Information Management");
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(subtitleLabel, "growx");
        
        // Login card
        JPanel loginCard = UIStyle.createCard();
        loginCard.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]8[]20[]8[]20[]15[]"));
        
        // Welcome text
        JLabel welcomeLabel = UIStyle.createHeading("Welcome Back", 2);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loginCard.add(welcomeLabel, "growx, wrap");
        
        JLabel signInPrompt = UIStyle.createBodyLabel("Sign in to access your dashboard");
        signInPrompt.setHorizontalAlignment(SwingConstants.CENTER);
        loginCard.add(signInPrompt, "growx, wrap");
        
        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(UIStyle.FONT_BODY_BOLD);
        usernameLabel.setForeground(UIStyle.TEXT_PRIMARY);
        loginCard.add(usernameLabel, "growx");
        loginCard.add(usernameField, "growx, h 50!, wrap");
        
        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(UIStyle.FONT_BODY_BOLD);
        passwordLabel.setForeground(UIStyle.TEXT_PRIMARY);
        loginCard.add(passwordLabel, "growx");
        loginCard.add(passwordField, "growx, h 50!, wrap");
        
        // Login button
        loginCard.add(loginButton, "growx, h 50!, wrap");
        
        // Status label
        loginCard.add(statusLabel, "growx, h 25!, wrap");
        
        // Footer
        JPanel footerPanel = new JPanel(new MigLayout("fillx, wrap", "[center]", "[]"));
        footerPanel.setOpaque(false);
        JLabel footerLabel = new JLabel("<html><center style='font-size: 12px; color: #888888;'>" +
            "© 2025 University ERP System<br/>" +
            "<span style='font-size: 11px; color: #666666;'>Secure Academic Management Platform</span></center></html>");
        footerLabel.setFont(UIStyle.FONT_CAPTION);
        footerPanel.add(footerLabel);
        
        // Assemble
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(loginCard, BorderLayout.CENTER);
        mainContainer.add(footerPanel, BorderLayout.SOUTH);
        
        setContentPane(mainContainer);
        
        // Attach listener
        loginButton.addActionListener(handleLogin());
        getRootPane().setDefaultButton(loginButton);
    }

    private void registerListeners() {
        // Additional listeners can be wired here if needed
    }

    private ActionListener handleLogin() {
        return event -> {
            String username = usernameField.getText().trim();
            char[] password = passwordField.getPassword();

            if (username.isEmpty() || password.length == 0) {
                showError("Please enter username and password.");
                return;
            }

            try {
                boolean success = authService.login(username, new String(password));
                if (success) {
                    User user = authService.getAuthenticatedUser();
                    showSuccess("Login successful! Redirecting...");
                    // Small delay for visual feedback
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            SwingUtilities.invokeLater(() -> openMainFrame(user));
                        } catch (InterruptedException e) {
                            SwingUtilities.invokeLater(() -> openMainFrame(user));
                        }
                    }).start();
                } else {
                    showError("Invalid credentials or inactive account.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Database error: " + ex.getMessage());
            }
        };
    }

    private void openMainFrame(User user) {
        setVisible(false);
        mainFrame.showForUser(user);
        mainFrame.setVisible(true);
        dispose();
    }

    private void showError(String message) {
        statusLabel.setText("⚠ " + message);
        statusLabel.setForeground(UIStyle.ERROR_RED);
        passwordField.setText("");
    }
    
    private void showSuccess(String message) {
        statusLabel.setText("✓ " + message);
        statusLabel.setForeground(UIStyle.SUCCESS_GREEN);
    }
}
