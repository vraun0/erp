package edu.univ.erp.ui.admin;

import edu.univ.erp.api.admin.AdminAPI;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

class UserManagementPanel extends JPanel {
    private final AdminAPI adminAPI;

    private final JTextField studentUsernameField = UIStyle.createTextField();
    private final JPasswordField studentPasswordField = UIStyle.createPasswordField();
    private final JTextField studentRollField = UIStyle.createTextField();
    private final JTextField studentProgramField = UIStyle.createTextField();
    private final JTextField studentYearField = UIStyle.createTextField();

    private final JTextField instructorUsernameField = UIStyle.createTextField();
    private final JPasswordField instructorPasswordField = UIStyle.createPasswordField();
    private final JTextField instructorDepartmentField = UIStyle.createTextField();

    UserManagementPanel(AdminAPI adminAPI) {
        this.adminAPI = adminAPI;
        setLayout(new MigLayout("fillx, wrap, insets 24", "[grow]", "[]24[]"));
        setBackground(UIStyle.BACKGROUND_DARK);

        // Student card with scrollable content
        JPanel studentCard = UIStyle.createCard();
        studentCard.setLayout(new BorderLayout());

        JPanel studentContent = new JPanel(
                new MigLayout("fillx, wrap, insets 0", "[grow]", "[]12[]10[]10[]10[]10[]10[]24[]"));
        studentContent.setOpaque(false);

        JLabel studentTitle = UIStyle.createHeading("Create Student Account", 3);
        studentContent.add(studentTitle, "growx, wrap");

        addFormField(studentContent, "👤 Username", studentUsernameField);
        addFormField(studentContent, "🔒 Password", studentPasswordField);
        addFormField(studentContent, "🎓 Roll Number", studentRollField);
        addFormField(studentContent, "📚 Program", studentProgramField);
        addFormField(studentContent, "📅 Year", studentYearField);

        JButton createStudentButton = UIStyle.createPrimaryButton("Create Student");
        createStudentButton.setPreferredSize(new Dimension(0, 48));
        createStudentButton.addActionListener(e -> createStudent());
        studentContent.add(createStudentButton, "growx");

        JScrollPane studentScroll = new JScrollPane(studentContent);
        studentScroll.setBorder(null);
        studentScroll.setOpaque(false);
        studentScroll.getViewport().setOpaque(false);
        studentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        studentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UIStyle.styleScrollPane(studentScroll);

        studentCard.add(studentScroll, BorderLayout.CENTER);
        studentCard.setPreferredSize(new Dimension(0, 520));
        add(studentCard, "growx, wrap");

        // Instructor card with scrollable content
        JPanel instructorCard = UIStyle.createCard();
        instructorCard.setLayout(new BorderLayout());

        JPanel instructorContent = new JPanel(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]12[]10[]10[]24[]"));
        instructorContent.setOpaque(false);

        JLabel instructorTitle = UIStyle.createHeading("Create Instructor Account", 3);
        instructorContent.add(instructorTitle, "growx, wrap");

        addFormField(instructorContent, "👤 Username", instructorUsernameField);
        addFormField(instructorContent, "🔒 Password", instructorPasswordField);
        addFormField(instructorContent, "🏛️ Department", instructorDepartmentField);

        JButton createInstructorButton = UIStyle.createPrimaryButton("Create Instructor");
        createInstructorButton.setPreferredSize(new Dimension(0, 48));
        createInstructorButton.addActionListener(e -> createInstructor());
        instructorContent.add(createInstructorButton, "growx");

        JScrollPane instructorScroll = new JScrollPane(instructorContent);
        instructorScroll.setBorder(null);
        instructorScroll.setOpaque(false);
        instructorScroll.getViewport().setOpaque(false);
        instructorScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        instructorScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UIStyle.styleScrollPane(instructorScroll);

        instructorCard.add(instructorScroll, BorderLayout.CENTER);
        instructorCard.setPreferredSize(new Dimension(0, 380));
        add(instructorCard, "growx");
    }

    private void addFormField(JPanel parent, String label, JComponent field) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(UIStyle.FONT_BODY_BOLD);
        labelComp.setForeground(UIStyle.TEXT_PRIMARY);
        parent.add(labelComp, "growx, width 160:160:");
        parent.add(field, "growx, h 48!, wrap");
    }

    private void createStudent() {
        try {
            String username = studentUsernameField.getText().trim();
            String password = new String(studentPasswordField.getPassword());
            String rollStr = studentRollField.getText().trim();
            String program = studentProgramField.getText().trim();
            String yearStr = studentYearField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || rollStr.isEmpty() || program.isEmpty()
                    || yearStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (username.length() < 3) {
                JOptionPane.showMessageDialog(this, "Username must be at least 3 characters long.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int rollNo;
            try {
                rollNo = Integer.parseInt(rollStr);
                if (rollNo <= 0)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Roll number must be a positive integer.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int year;
            try {
                year = Integer.parseInt(yearStr);
                if (year < 1 || year > 6) {
                    JOptionPane.showMessageDialog(this, "Year must be between 1 and 6.", "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Year must be a valid number.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            var response = adminAPI.createStudent(username, password, rollNo,
                    program, year);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Student account created successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                studentUsernameField.setText("");
                studentPasswordField.setText("");
                studentRollField.setText("");
                studentProgramField.setText("");
                studentYearField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format.", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void createInstructor() {
        try {
            String username = instructorUsernameField.getText().trim();
            String password = new String(instructorPasswordField.getPassword());
            String department = instructorDepartmentField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || department.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (username.length() < 3) {
                JOptionPane.showMessageDialog(this, "Username must be at least 3 characters long.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            var response = adminAPI.createInstructor(username, password,
                    department);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Instructor account created successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                instructorUsernameField.setText("");
                instructorPasswordField.setText("");
                instructorDepartmentField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
