package edu.univ.erp.ui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern ERP UI styling utilities with dark/blue theme
 * Inspired by React/Next.js dashboards with Material Design aesthetics
 */
public class UIStyle {
    // Modern ERP Dark/Blue Color Palette
    public static final Color NAVY_BLUE = new Color(15, 23, 42); // #0f172a - Deep navy background
    public static final Color DEEP_GRAY = new Color(30, 41, 59); // #1e293b - Sidebar/panels
    public static final Color ACCENT_BLUE = new Color(59, 130, 246); // #3b82f6 - Primary accent
    public static final Color ACCENT_BLUE_HOVER = new Color(96, 165, 250); // #60a5fa - Accent hover
    public static final Color BACKGROUND_DARK = new Color(15, 23, 42); // #0f172a - Main background
    public static final Color CARD_BACKGROUND = new Color(30, 41, 59); // #1e293b - Card background
    public static final Color CARD_HOVER = new Color(51, 65, 85); // #334155 - Card hover
    public static final Color BORDER_COLOR = new Color(51, 65, 85); // #334155 - Borders
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252); // #f8fafc - Primary text
    public static final Color TEXT_SECONDARY = new Color(203, 213, 225); // #cbd5e1 - Secondary text
    public static final Color TEXT_MUTED = new Color(148, 163, 184); // #94a3b8 - Muted text

    // Semantic Colors
    public static final Color SUCCESS_GREEN = new Color(34, 197, 94); // #22c55e
    public static final Color WARNING_ORANGE = new Color(251, 146, 60); // #fb923c
    public static final Color ERROR_RED = new Color(239, 68, 68); // #ef4444
    public static final Color INFO_BLUE = new Color(59, 130, 246); // #3b82f6

    // Sidebar Colors
    public static final Color SIDEBAR_BG = new Color(30, 41, 59); // #1e293b
    public static final Color SIDEBAR_HOVER = new Color(51, 65, 85); // #334155
    public static final Color SIDEBAR_ACTIVE = new Color(59, 130, 246); // #3b82f6
    public static final Color SIDEBAR_TEXT = new Color(203, 213, 225); // #cbd5e1

    // Fonts - Modern, clean typography
    public static final String FONT_FAMILY = "Segoe UI";
    public static final Font FONT_H1 = new Font(FONT_FAMILY, Font.BOLD, 32);
    public static final Font FONT_H2 = new Font(FONT_FAMILY, Font.BOLD, 24);
    public static final Font FONT_H3 = new Font(FONT_FAMILY, Font.BOLD, 20);
    public static final Font FONT_H4 = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_CAPTION = new Font(FONT_FAMILY, Font.PLAIN, 11);

    // Dimensions
    public static final int SIDEBAR_WIDTH = 260;
    public static final int SIDEBAR_COLLAPSED_WIDTH = 70;
    public static final int TOPBAR_HEIGHT = 64;
    public static final int CARD_RADIUS = 12;
    public static final int BUTTON_RADIUS = 8;

    /**
     * Create a modern card panel with rounded corners and shadow
     */
    public static JPanel createCard() {
        RoundedPanel card = new RoundedPanel(CARD_RADIUS, CARD_BACKGROUND);
        card.setBorder(new EmptyBorder(28, 28, 32, 28));
        return card;
    }

    /**
     * Create a gradient card panel
     */
    public static JPanel createGradientCard(Color startColor, Color endColor) {
        return new GradientPanel(startColor, endColor, CARD_RADIUS);
    }

    /**
     * Create a heading label
     */
    public static JLabel createHeading(String text, int level) {
        JLabel label = new JLabel(text);
        switch (level) {
            case 1 -> {
                label.setFont(FONT_H1);
                label.setForeground(TEXT_PRIMARY);
            }
            case 2 -> {
                label.setFont(FONT_H2);
                label.setForeground(TEXT_PRIMARY);
            }
            case 3 -> {
                label.setFont(FONT_H3);
                label.setForeground(TEXT_PRIMARY);
            }
            case 4 -> {
                label.setFont(FONT_H4);
                label.setForeground(TEXT_PRIMARY);
            }
            default -> {
                label.setFont(FONT_BODY_BOLD);
                label.setForeground(TEXT_PRIMARY);
            }
        }
        return label;
    }

    /**
     * Create a body text label
     */
    public static JLabel createBodyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    /**
     * Create a modern rounded button with hover effects
     */
    public static JButton createPrimaryButton(String text) {
        return new RoundedButton(text, ACCENT_BLUE, Color.WHITE, BUTTON_RADIUS);
    }

    /**
     * Create a secondary button
     */
    public static JButton createSecondaryButton(String text) {
        return new RoundedButton(text, CARD_BACKGROUND, TEXT_PRIMARY, BUTTON_RADIUS);
    }

    /**
     * Create a modern text field with rounded corners
     */
    public static JTextField createTextField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BUTTON_RADIUS, BUTTON_RADIUS);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        field.setFont(FONT_BODY);
        field.setBackground(CARD_BACKGROUND);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_BLUE);
        field.setBorder(new EmptyBorder(14, 18, 14, 18));
        field.setOpaque(false);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBackground(new Color(CARD_BACKGROUND.getRGB()));
                field.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBackground(CARD_BACKGROUND);
                field.repaint();
            }
        });

        return field;
    }

    /**
     * Create a modern password field with rounded corners
     */
    public static JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BUTTON_RADIUS, BUTTON_RADIUS);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        field.setFont(FONT_BODY);
        field.setBackground(CARD_BACKGROUND);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_BLUE);
        field.setBorder(new EmptyBorder(14, 18, 14, 18));
        field.setOpaque(false);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBackground(new Color(CARD_BACKGROUND.getRGB()));
                field.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBackground(CARD_BACKGROUND);
                field.repaint();
            }
        });

        return field;
    }

    /**
     * Style a table for modern ERP look
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setBackground(CARD_BACKGROUND);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(ACCENT_BLUE);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(52);
        table.setShowGrid(true);
        table.setGridColor(new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(), BORDER_COLOR.getBlue(), 100));
        table.setIntercellSpacing(new Dimension(2, 2));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(true);
        table.setAutoCreateRowSorter(true);

        if (table.getTableHeader() != null) {
            table.getTableHeader().setFont(FONT_BODY_BOLD);
            table.getTableHeader().setBackground(DEEP_GRAY);
            table.getTableHeader().setForeground(TEXT_PRIMARY);
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setPreferredSize(new Dimension(0, 56));
            table.getTableHeader().setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        }
    }

    /**
     * Apply modern styling to scroll pane
     */
    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(new EmptyBorder(24, 24, 24, 24));
        scrollPane.getViewport().setBackground(BACKGROUND_DARK);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Custom scrollbar styling
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setBackground(DEEP_GRAY);
        vertical.setForeground(BORDER_COLOR);
        vertical.setPreferredSize(new Dimension(10, 0));
        vertical.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = BORDER_COLOR;
                this.trackColor = DEEP_GRAY;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
    }

    // Gradients
    public static final Color GRADIENT_START = new Color(15, 23, 42); // #0f172a
    public static final Color GRADIENT_END = new Color(30, 58, 138); // #1e3a8a

    /**
     * Create a panel with a drop shadow effect
     */
    public static JPanel createDropShadowPanel() {
        return new DropShadowPanel(CARD_RADIUS, CARD_BACKGROUND);
    }
}

/**
 * Custom rounded panel with optional gradient
 */
class RoundedPanel extends JPanel {
    private final int radius;

    public RoundedPanel(int radius, Color backgroundColor) {
        this.radius = radius;
        setBackground(backgroundColor);
        setOpaque(false);
        // Ensure minimum size to prevent clipping
        setMinimumSize(new Dimension(200, 100));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        if (pref.width < 200)
            pref.width = 200;
        if (pref.height < 100)
            pref.height = 100;
        return pref;
    }
}

/**
 * Gradient panel with rounded corners
 */
class GradientPanel extends JPanel {
    private final Color startColor;
    private final Color endColor;
    private final int radius;

    public GradientPanel(Color startColor, Color endColor, int radius) {
        this.startColor = startColor;
        this.endColor = endColor;
        this.radius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
                0, 0, startColor,
                getWidth(), getHeight(), endColor);
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        g2.dispose();
    }
}

/**
 * Panel with a soft drop shadow
 */
class DropShadowPanel extends JPanel {
    private final int radius;
    private final Color backgroundColor;
    private final int shadowSize = 6;

    public DropShadowPanel(int radius, Color backgroundColor) {
        this.radius = radius;
        this.backgroundColor = backgroundColor;
        setOpaque(false);
        setBorder(new EmptyBorder(shadowSize, shadowSize, shadowSize, shadowSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw shadow
        g2.setColor(new Color(0, 0, 0, 50));
        for (int i = 0; i < shadowSize; i++) {
            g2.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, radius, radius);
        }

        // Draw main panel
        g2.setColor(backgroundColor);
        g2.fillRoundRect(shadowSize, shadowSize, getWidth() - shadowSize * 2, getHeight() - shadowSize * 2, radius,
                radius);

        g2.dispose();
    }
}

/**
 * Rounded button with hover effects
 */
class RoundedButton extends JButton {
    private final int radius;
    private Color bgColor;
    private Color hoverColor;

    public RoundedButton(String text, Color bgColor, Color textColor, int radius) {
        super(text);
        this.bgColor = bgColor;
        this.hoverColor = bgColor.brighter();
        this.radius = radius;

        setFont(UIStyle.FONT_BODY_BOLD);
        setForeground(textColor);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(14, 28, 14, 28));
        setPreferredSize(new Dimension(0, 44));

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                hoverColor = bgColor.brighter();
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hoverColor = bgColor;
                repaint();
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                hoverColor = bgColor.darker();
                repaint();
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                hoverColor = bgColor.brighter();
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed()) {
            g2.setColor(bgColor.darker());
        } else if (getModel().isRollover()) {
            g2.setColor(hoverColor);
        } else {
            g2.setColor(bgColor);
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();

        super.paintComponent(g);
    }
}
