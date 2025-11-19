package com.sis.app;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.sis.app.service.AuthService;
import com.sis.app.ui.LoginFrame;
import com.sis.app.ui.MainFrame;
import com.sis.app.ui.util.UIStyle;

import javax.swing.*;
import java.awt.*;

/**
 * Application entry point with modern ERP UI theme.
 */
public class SISApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set modern dark theme
                UIManager.setLookAndFeel(new FlatDarculaLaf());
                
                // Custom blue/black theme customization
                UIManager.put("Button.arc", 6);
                UIManager.put("TextComponent.arc", 6);
                UIManager.put("ProgressBar.arc", 6);
                UIManager.put("Component.arc", 6);
                UIManager.put("Component.focusWidth", 2);
                UIManager.put("Component.innerFocusWidth", 1);
                
                // Modern ERP Dark/Blue color scheme
                UIManager.put("Button.default.background", UIStyle.ACCENT_BLUE);
                UIManager.put("Button.default.foreground", Color.WHITE);
                UIManager.put("Button.default.focusColor", UIStyle.ACCENT_BLUE_HOVER);
                
                UIManager.put("TabbedPane.selectedBackground", UIStyle.BACKGROUND_DARK);
                UIManager.put("TabbedPane.selectedForeground", UIStyle.TEXT_PRIMARY);
                UIManager.put("TabbedPane.underlineColor", UIStyle.ACCENT_BLUE);
                UIManager.put("TabbedPane.contentAreaColor", UIStyle.BACKGROUND_DARK);
                
                UIManager.put("Table.background", UIStyle.CARD_BACKGROUND);
                UIManager.put("Table.foreground", UIStyle.TEXT_PRIMARY);
                UIManager.put("Table.selectionBackground", UIStyle.ACCENT_BLUE);
                UIManager.put("Table.selectionForeground", Color.WHITE);
                UIManager.put("Table.gridColor", UIStyle.BORDER_COLOR);
                UIManager.put("TableHeader.background", UIStyle.DEEP_GRAY);
                UIManager.put("TableHeader.foreground", UIStyle.TEXT_PRIMARY);
                
                UIManager.put("Panel.background", UIStyle.BACKGROUND_DARK);
                UIManager.put("Label.foreground", UIStyle.TEXT_PRIMARY);
                UIManager.put("TextField.background", UIStyle.CARD_BACKGROUND);
                UIManager.put("TextField.foreground", UIStyle.TEXT_PRIMARY);
                UIManager.put("TextField.borderColor", UIStyle.BORDER_COLOR);
                UIManager.put("TextField.focusColor", UIStyle.ACCENT_BLUE);
                
                UIManager.put("PasswordField.background", UIStyle.CARD_BACKGROUND);
                UIManager.put("PasswordField.foreground", UIStyle.TEXT_PRIMARY);
                UIManager.put("PasswordField.borderColor", UIStyle.BORDER_COLOR);
                UIManager.put("PasswordField.focusColor", UIStyle.ACCENT_BLUE);
                
                UIManager.put("MenuBar.background", UIStyle.DEEP_GRAY);
                UIManager.put("MenuBar.foreground", UIStyle.TEXT_PRIMARY);
                UIManager.put("Menu.background", UIStyle.DEEP_GRAY);
                UIManager.put("Menu.foreground", UIStyle.TEXT_PRIMARY);
                UIManager.put("MenuItem.background", UIStyle.DEEP_GRAY);
                UIManager.put("MenuItem.foreground", UIStyle.TEXT_PRIMARY);
                
                // Larger default font
                Font defaultFont = UIStyle.FONT_BODY;
                UIManager.put("Label.font", defaultFont);
                UIManager.put("Button.font", defaultFont);
                UIManager.put("TextField.font", defaultFont);
                UIManager.put("PasswordField.font", defaultFont);
                UIManager.put("MenuBar.font", UIStyle.FONT_BODY);
                UIManager.put("Menu.font", UIStyle.FONT_BODY);
                UIManager.put("MenuItem.font", UIStyle.FONT_BODY);
                
            } catch (Exception e) {
                e.printStackTrace();
            }

            AuthService authService = new AuthService();
            MainFrame mainFrame = new MainFrame();
            LoginFrame loginFrame = new LoginFrame(authService, mainFrame);

            loginFrame.setVisible(true);
        });
    }
}
