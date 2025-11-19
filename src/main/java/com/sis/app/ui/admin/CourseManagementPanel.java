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
        setLayout(new MigLayout("fillx, wrap, insets 20", "[grow]", "[]"));
        setBackground(UIStyle.BACKGROUND_DARK);
        
        JPanel courseCard = UIStyle.createCard();
        courseCard.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]8[]8[]8[]15[]"));
        
        JLabel title = UIStyle.createHeading("Create New Course", 3);
        courseCard.add(title, "growx, wrap");
        
        addFormField(courseCard, "📋 Course Code", codeField);
        addFormField(courseCard, "📖 Course Title", titleField);
        addFormField(courseCard, "🎓 Credits", creditsField);
        
        JButton createButton = UIStyle.createPrimaryButton("Create Course");
        createButton.setPreferredSize(new Dimension(0, 45));
        createButton.addActionListener(e -> createCourse());
        courseCard.add(createButton, "growx");
        
        add(courseCard, "growx");
    }
    
    private void addFormField(JPanel parent, String label, JComponent field) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(UIStyle.FONT_BODY_BOLD);
        labelComp.setForeground(UIStyle.TEXT_PRIMARY);
        parent.add(labelComp, "growx");
        parent.add(field, "growx, h 45!, wrap");
    }

    private void createCourse() {
        try {
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            int credits = Integer.parseInt(creditsField.getText().trim());
            adminService.createCourse(code, title, credits);
            JOptionPane.showMessageDialog(this, "Course created successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            codeField.setText("");
            titleField.setText("");
            creditsField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Credits must be numeric.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

