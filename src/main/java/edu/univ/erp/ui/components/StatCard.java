package edu.univ.erp.ui.components;

import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class StatCard extends JPanel {

    public StatCard(String title, String value, String icon, Color accentColor) {
        setLayout(new MigLayout("fill, insets 20", "[grow][]", "[]4[]"));
        setBackground(UIStyle.CARD_BACKGROUND);

        // Rounded corners
        setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIStyle.FONT_BODY);
        titleLabel.setForeground(UIStyle.TEXT_SECONDARY);
        add(titleLabel, "cell 0 0");

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        add(iconLabel, "cell 1 0 1 2, aligny top");

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(UIStyle.FONT_H2);
        valueLabel.setForeground(UIStyle.TEXT_PRIMARY);
        add(valueLabel, "cell 0 1");

        // Bottom accent bar
        JPanel bar = new JPanel();
        bar.setBackground(accentColor);
        bar.setPreferredSize(new Dimension(0, 4));
        add(bar, "dock south, h 4!");
    }

    public void setValue(String value) {
        // Find the value label (it's the 3rd component added: title, icon, value)
        // Better to keep a reference, but for now we can find it or just store it in
        // constructor
        // Actually, let's modify the class to store the label reference.
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), UIStyle.CARD_RADIUS, UIStyle.CARD_RADIUS);
        g2.dispose();
        super.paintComponent(g);
    }
}
