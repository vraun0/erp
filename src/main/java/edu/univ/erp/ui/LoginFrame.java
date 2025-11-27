package edu.univ.erp.ui;

import edu.univ.erp.model.User;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.ui.util.UIStyle;
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
        setSize(1024, 768); // Increased default size for split layout
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(800, 600)); // Increased minimum size
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

        // Split Pane Container
        JPanel splitContainer = new JPanel(new MigLayout("fill, insets 0", "[45%][55%]", "[grow]"));

        // Left Side - Branding (Gradient)
        JPanel brandingPanel = UIStyle.createGradientCard(UIStyle.GRADIENT_START, UIStyle.GRADIENT_END);
        brandingPanel.setLayout(new MigLayout("fill, insets 40", "[center]", "push[]10[]20[]push"));

        JLabel logoLabel = new JLabel("🎓");
        logoLabel.setFont(new Font(UIStyle.FONT_FAMILY, Font.PLAIN, 80));
        brandingPanel.add(logoLabel, "wrap");

        JLabel titleLabel = new JLabel("University ERP");
        titleLabel.setFont(UIStyle.FONT_H1);
        titleLabel.setForeground(Color.WHITE);
        brandingPanel.add(titleLabel, "wrap");

        JLabel subtitleLabel = new JLabel(
                "<html><center>Secure Academic Management<br>Platform for Students & Faculty</center></html>");
        subtitleLabel.setFont(UIStyle.FONT_H4);
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        brandingPanel.add(subtitleLabel, "wrap");

        // Right Side - Login Form
        JPanel loginPanel = new JPanel(new MigLayout("fill, insets 60 40 60 40", "[grow]", "push[]20[]20[]push"));
        loginPanel.setBackground(UIStyle.BACKGROUND_DARK);

        // Login Card with Shadow
        JPanel loginCard = UIStyle.createDropShadowPanel();
        loginCard.setLayout(new MigLayout("fillx, wrap, insets 30", "[grow]", "[]8[]30[]8[]20[]8[]30[]"));

        JLabel welcomeLabel = UIStyle.createHeading("Welcome Back", 2);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loginCard.add(welcomeLabel, "growx, wrap");

        JLabel signInPrompt = UIStyle.createBodyLabel("Sign in to your account");
        signInPrompt.setHorizontalAlignment(SwingConstants.CENTER);
        loginCard.add(signInPrompt, "growx, wrap");

        // Username
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(UIStyle.FONT_BODY_BOLD);
        usernameLabel.setForeground(UIStyle.TEXT_PRIMARY);
        loginCard.add(usernameLabel, "growx");
        loginCard.add(usernameField, "growx, h 50!, wrap");

        // Password
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(UIStyle.FONT_BODY_BOLD);
        passwordLabel.setForeground(UIStyle.TEXT_PRIMARY);
        loginCard.add(passwordLabel, "growx");
        loginCard.add(passwordField, "growx, h 50!, wrap");

        // Button
        loginCard.add(loginButton, "growx, h 50!, wrap");

        // Status
        loginCard.add(statusLabel, "growx, h 20!, wrap");

        // Responsive width for login card
        loginPanel.add(loginCard, "growx, width 350:420:500");

        // Footer in Login Panel
        JLabel footerLabel = new JLabel("© 2025 University ERP System");
        footerLabel.setFont(UIStyle.FONT_CAPTION);
        footerLabel.setForeground(UIStyle.TEXT_MUTED);
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loginPanel.add(footerLabel, "dock south, gapbottom 20");

        splitContainer.add(brandingPanel, "grow");
        splitContainer.add(loginPanel, "grow");

        add(splitContainer, BorderLayout.CENTER);

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
        try {
            // Hide login frame first
            setVisible(false);

            // Setup and show main frame
            mainFrame.showForUser(user);

            // Ensure main frame is visible and properly displayed
            mainFrame.setExtendedState(JFrame.NORMAL);
            mainFrame.setVisible(true);
            mainFrame.toFront();
            mainFrame.requestFocus();
            mainFrame.revalidate();
            mainFrame.repaint();

            // Dispose login frame after a short delay to ensure smooth transition
            SwingUtilities.invokeLater(() -> {
                dispose();
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Failed to open main window: " + ex.getMessage());
            setVisible(true);
            mainFrame.setVisible(false);
        }
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
