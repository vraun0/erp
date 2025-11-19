package com.sis.app.ui.components;

import com.sis.app.model.User;
import com.sis.app.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Fixed top bar with app name, search, and user profile
 */
public class TopBarPanel extends JPanel {
    private final JTextField searchField;
    private JLabel userLabel;
    private JButton profileButton;
    
    public TopBarPanel() {
        setLayout(new MigLayout("fillx, insets 0 24 0 24", "[grow][][]", "[]"));
        setPreferredSize(new Dimension(0, UIStyle.TOPBAR_HEIGHT));
        setBackground(UIStyle.DEEP_GRAY);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.BORDER_COLOR));
        
        // App name/title
        JLabel appTitle = new JLabel("Dashboard");
        appTitle.setFont(UIStyle.FONT_H3);
        appTitle.setForeground(UIStyle.TEXT_PRIMARY);
        add(appTitle, "growx 0, gapright 24");
        
        // Search bar
        searchField = createSearchField();
        add(searchField, "w 320!, h 38!, growx");
        
        // User profile section
        JPanel profilePanel = createProfilePanel();
        add(profilePanel, "growx 0");
    }
    
    private JTextField createSearchField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        
        field.setFont(UIStyle.FONT_BODY);
        field.setBackground(UIStyle.CARD_BACKGROUND);
        field.setForeground(UIStyle.TEXT_PRIMARY);
        field.setCaretColor(UIStyle.ACCENT_BLUE);
        field.setBorder(new EmptyBorder(10, 40, 10, 16));
        field.putClientProperty("JTextField.placeholderText", "Search...");
        field.setOpaque(false);
        
        return field;
    }
    
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
        panel.setOpaque(false);
        
        // User info
        userLabel = new JLabel("User");
        userLabel.setFont(UIStyle.FONT_BODY);
        userLabel.setForeground(UIStyle.TEXT_PRIMARY);
        panel.add(userLabel, "growx 0, gapright 12");
        
        // Profile button/avatar
        profileButton = new JButton("👤") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isRollover()) {
                    g2.setColor(UIStyle.CARD_HOVER);
                } else {
                    g2.setColor(UIStyle.CARD_BACKGROUND);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        profileButton.setFont(new Font(UIStyle.FONT_FAMILY, Font.PLAIN, 20));
        profileButton.setPreferredSize(new Dimension(40, 40));
        profileButton.setContentAreaFilled(false);
        profileButton.setFocusPainted(false);
        profileButton.setBorderPainted(false);
        profileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileButton.setOpaque(false);
        
        panel.add(profileButton);
        
        return panel;
    }
    
    public void setUser(User user) {
        if (user != null && userLabel != null) {
            String role = user.isAdmin() ? "Admin" : user.isInstructor() ? "Instructor" : "Student";
            userLabel.setText(user.getUsername() + " • " + role);
        }
    }
    
    public void setTitle(String title) {
        Component[] components = getComponents();
        if (components.length > 0 && components[0] instanceof JLabel) {
            ((JLabel) components[0]).setText(title);
        }
    }
    
    public void setSearchListener(ActionListener listener) {
        searchField.addActionListener(listener);
    }
    
    public void setProfileListener(ActionListener listener) {
        profileButton.addActionListener(listener);
    }
    
    public String getSearchText() {
        return searchField.getText();
    }
}

