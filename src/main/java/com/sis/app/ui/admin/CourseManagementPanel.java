package com.sis.app.ui.admin;

import com.sis.app.service.AdminService;
import com.sis.app.service.ServiceException;
import com.sis.app.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

class CourseManagementPanel extends JPanel {
    private final AdminService adminService;
    private final JTextField codeField = UIStyle.createTextField();
    private final JTextField titleField = UIStyle.createTextField();
    private final JTextField creditsField = UIStyle.createTextField();

    CourseManagementPanel(AdminService adminService) {
        this.adminService = adminService;
        setLayout(new MigLayout("fillx, wrap, insets 24", "[grow]", "[]"));
        setBackground(UIStyle.BACKGROUND_DARK);

        JPanel courseCard = UIStyle.createCard();
        courseCard.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]12[]10[]10[]10[]22[]"));

        JLabel title = UIStyle.createHeading("Create New Course", 3);
        courseCard.add(title, "growx, wrap");

        addFormField(courseCard, "📋 Course Code", codeField);
        addFormField(courseCard, "📖 Course Title", titleField);
        addFormField(courseCard, "🎓 Credits", creditsField);

        JButton createButton = UIStyle.createPrimaryButton("Create Course");
        createButton.setPreferredSize(new Dimension(0, 48));
        createButton.addActionListener(e -> createCourse());
        courseCard.add(createButton, "growx");

        add(courseCard, "growx");
    }

    private void addFormField(JPanel parent, String label, JComponent field) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(UIStyle.FONT_BODY_BOLD);
        labelComp.setForeground(UIStyle.TEXT_PRIMARY);
        parent.add(labelComp, "growx, width 160:160:");
        parent.add(field, "growx, h 48!, wrap");
    }

    private void createCourse() {
        try {
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            String creditsStr = creditsField.getText().trim();

            if (code.isEmpty() || title.isEmpty() || creditsStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!code.matches("[a-zA-Z0-9]+")) {
                JOptionPane.showMessageDialog(this, "Course code must be alphanumeric.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int credits;
            try {
                credits = Integer.parseInt(creditsStr);
                if (credits < 1 || credits > 10) {
                    JOptionPane.showMessageDialog(this, "Credits must be between 1 and 10.", "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Credits must be a valid number.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            adminService.createCourse(code, title, credits);
            JOptionPane.showMessageDialog(this, "Course created successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            codeField.setText("");
            titleField.setText("");
            creditsField.setText("");
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
