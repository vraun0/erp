package com.sis.app.ui.admin;

import com.sis.app.service.AdminService;
import com.sis.app.service.ServiceException;
import com.sis.app.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

class UserManagementPanel extends JPanel {
    private final AdminService adminService;

    private final JTextField studentUsernameField = UIStyle.createTextField();
    private final JPasswordField studentPasswordField = UIStyle.createPasswordField();
    private final JTextField studentRollField = UIStyle.createTextField();
    private final JTextField studentProgramField = UIStyle.createTextField();
    private final JTextField studentYearField = UIStyle.createTextField();

    private final JTextField instructorUsernameField = UIStyle.createTextField();
    private final JPasswordField instructorPasswordField = UIStyle.createPasswordField();
    private final JTextField instructorDepartmentField = UIStyle.createTextField();

    UserManagementPanel(AdminService adminService) {
        this.adminService = adminService;
        setLayout(new MigLayout("fillx, wrap, insets 20", "[grow]", "[]20[]"));
        setBackground(UIStyle.BACKGROUND_DARK);
        
        // Student card
        JPanel studentCard = UIStyle.createCard();
        studentCard.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]8[]8[]8[]8[]8[]15[]"));
        
        JLabel studentTitle = UIStyle.createHeading("Create Student Account", 3);
        studentCard.add(studentTitle, "growx, wrap");
        
        addFormField(studentCard, "👤 Username", studentUsernameField);
        addFormField(studentCard, "🔒 Password", studentPasswordField);
        addFormField(studentCard, "🎓 Roll Number", studentRollField);
        addFormField(studentCard, "📚 Program", studentProgramField);
        addFormField(studentCard, "📅 Year", studentYearField);
        
        JButton createStudentButton = UIStyle.createPrimaryButton("Create Student");
        createStudentButton.setPreferredSize(new Dimension(0, 45));
        createStudentButton.addActionListener(e -> createStudent());
        studentCard.add(createStudentButton, "growx");
        
        add(studentCard, "growx, wrap");
        
        // Instructor card
        JPanel instructorCard = UIStyle.createCard();
        instructorCard.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]8[]8[]8[]15[]"));
        
        JLabel instructorTitle = UIStyle.createHeading("Create Instructor Account", 3);
        instructorCard.add(instructorTitle, "growx, wrap");
        
        addFormField(instructorCard, "👤 Username", instructorUsernameField);
        addFormField(instructorCard, "🔒 Password", instructorPasswordField);
        addFormField(instructorCard, "🏛️ Department", instructorDepartmentField);
        
        JButton createInstructorButton = UIStyle.createPrimaryButton("Create Instructor");
        createInstructorButton.setPreferredSize(new Dimension(0, 45));
        createInstructorButton.addActionListener(e -> createInstructor());
        instructorCard.add(createInstructorButton, "growx");
        
        add(instructorCard, "growx");
    }
    
    private void addFormField(JPanel parent, String label, JComponent field) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(UIStyle.FONT_BODY_BOLD);
        labelComp.setForeground(UIStyle.TEXT_PRIMARY);
        parent.add(labelComp, "growx");
        parent.add(field, "growx, h 45!, wrap");
    }

    private void createStudent() {
        try {
            String username = studentUsernameField.getText().trim();
            String password = new String(studentPasswordField.getPassword());
            int rollNo = Integer.parseInt(studentRollField.getText().trim());
            String program = studentProgramField.getText().trim();
            int year = Integer.parseInt(studentYearField.getText().trim());
            adminService.createStudentAccount(username, password, rollNo, program, year);
            JOptionPane.showMessageDialog(this, "Student account created successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            studentUsernameField.setText("");
            studentPasswordField.setText("");
            studentRollField.setText("");
            studentProgramField.setText("");
            studentYearField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Roll number and year must be numeric.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createInstructor() {
        try {
            String username = instructorUsernameField.getText().trim();
            String password = new String(instructorPasswordField.getPassword());
            String department = instructorDepartmentField.getText().trim();
            adminService.createInstructorAccount(username, password, department);
            JOptionPane.showMessageDialog(this, "Instructor account created successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            instructorUsernameField.setText("");
            instructorPasswordField.setText("");
            instructorDepartmentField.setText("");
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

