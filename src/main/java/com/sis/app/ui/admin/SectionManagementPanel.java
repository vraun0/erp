package com.sis.app.ui.admin;

import com.sis.app.model.Course;
import com.sis.app.model.Instructor;
import com.sis.app.model.Section;
import com.sis.app.service.AdminService;
import com.sis.app.service.ServiceException;
import com.sis.app.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

class SectionManagementPanel extends JPanel {
    private final AdminService adminService;

    private final JComboBox<Course> courseCombo = createStyledCombo();
    private final JComboBox<Instructor> instructorCombo = createStyledCombo();
    private final JTextField dayTimeField = UIStyle.createTextField();
    private final JTextField roomField = UIStyle.createTextField();
    private final JTextField capacityField = UIStyle.createTextField();
    private final JTextField semesterField = UIStyle.createTextField();
    private final JTextField yearField = UIStyle.createTextField();

    private final JComboBox<Section> sectionCombo = createStyledCombo();
    private final JComboBox<Instructor> assignInstructorCombo = createStyledCombo();

    SectionManagementPanel(AdminService adminService) {
        this.adminService = adminService;
        setLayout(new MigLayout("fillx, wrap, insets 20", "[grow]", "[]20[]"));
        setBackground(UIStyle.BACKGROUND_DARK);

        // Create Section Card
        JPanel createSectionCard = UIStyle.createCard();
        createSectionCard.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", 
            "[]8[]8[]8[]8[]8[]8[]8[]15[]"));
        
        JLabel createTitle = UIStyle.createHeading("Create New Section", 3);
        createSectionCard.add(createTitle, "growx, wrap");
        
        addComboField(createSectionCard, "📚 Course", courseCombo);
        addComboField(createSectionCard, "👨‍🏫 Instructor", instructorCombo);
        addFormField(createSectionCard, "📅 Day & Time", dayTimeField);
        addFormField(createSectionCard, "🏢 Room", roomField);
        addFormField(createSectionCard, "👥 Capacity", capacityField);
        addFormField(createSectionCard, "📆 Semester", semesterField);
        addFormField(createSectionCard, "📅 Year", yearField);
        
        JButton createSectionButton = UIStyle.createPrimaryButton("Create Section");
        createSectionButton.setPreferredSize(new Dimension(0, 45));
        createSectionButton.addActionListener(e -> createSection());
        createSectionCard.add(createSectionButton, "growx");
        
        add(createSectionCard, "growx, wrap");

        // Assign Instructor Card
        JPanel assignCard = UIStyle.createCard();
        assignCard.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]8[]8[]8[]15[]"));
        
        JLabel assignTitle = UIStyle.createHeading("Assign Instructor to Section", 3);
        assignCard.add(assignTitle, "growx, wrap");
        
        addComboField(assignCard, "📖 Section", sectionCombo);
        addComboField(assignCard, "👨‍🏫 Instructor", assignInstructorCombo);
        
        JButton assignButton = UIStyle.createPrimaryButton("Assign Instructor");
        assignButton.setPreferredSize(new Dimension(0, 45));
        assignButton.addActionListener(e -> assignInstructor());
        assignCard.add(assignButton, "growx");
        
        add(assignCard, "growx");
    }
    
    private <T> JComboBox<T> createStyledCombo() {
        JComboBox<T> combo = new JComboBox<>();
        combo.setFont(UIStyle.FONT_BODY);
        combo.setBackground(UIStyle.CARD_BACKGROUND);
        combo.setForeground(UIStyle.TEXT_PRIMARY);
        combo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(UIStyle.BORDER_COLOR, 1),
            javax.swing.BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setFont(UIStyle.FONT_BODY);
                c.setForeground(isSelected ? Color.WHITE : UIStyle.TEXT_PRIMARY);
                c.setBackground(isSelected ? UIStyle.ACCENT_BLUE : UIStyle.CARD_BACKGROUND);
                return c;
            }
        });
        return combo;
    }
    
    private void addFormField(JPanel parent, String label, JComponent field) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(UIStyle.FONT_BODY_BOLD);
        labelComp.setForeground(UIStyle.TEXT_PRIMARY);
        parent.add(labelComp, "growx");
        parent.add(field, "growx, h 45!, wrap");
    }
    
    private void addComboField(JPanel parent, String label, JComboBox<?> combo) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(UIStyle.FONT_BODY_BOLD);
        labelComp.setForeground(UIStyle.TEXT_PRIMARY);
        parent.add(labelComp, "growx");
        parent.add(combo, "growx, h 45!, wrap");
    }

    void refreshData() {
        try {
            List<Course> courses = adminService.getAllCourses();
            courseCombo.setModel(new DefaultComboBoxModel<>(courses.toArray(new Course[0])));

            List<Instructor> instructors = adminService.getAllInstructors();
            instructorCombo.setModel(new DefaultComboBoxModel<>(instructors.toArray(new Instructor[0])));
            assignInstructorCombo.setModel(new DefaultComboBoxModel<>(instructors.toArray(new Instructor[0])));

            List<Section> sections = adminService.getAllSections();
            sectionCombo.setModel(new DefaultComboBoxModel<>(sections.toArray(new Section[0])));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load data: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createSection() {
        Course course = (Course) courseCombo.getSelectedItem();
        Instructor instructor = (Instructor) instructorCombo.getSelectedItem();
        if (course == null || instructor == null) {
            JOptionPane.showMessageDialog(this, "Please select both course and instructor.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int capacity = Integer.parseInt(capacityField.getText().trim());
            int semester = Integer.parseInt(semesterField.getText().trim());
            int year = Integer.parseInt(yearField.getText().trim());
            adminService.createSection(
                    course.getCode(),
                    instructor.getUserId(),
                    dayTimeField.getText().trim(),
                    roomField.getText().trim(),
                    capacity,
                    semester,
                    year
            );
            JOptionPane.showMessageDialog(this, "Section created successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dayTimeField.setText("");
            roomField.setText("");
            capacityField.setText("");
            semesterField.setText("");
            yearField.setText("");
            refreshData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Capacity, semester, and year must be numeric.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void assignInstructor() {
        Section section = (Section) sectionCombo.getSelectedItem();
        Instructor instructor = (Instructor) assignInstructorCombo.getSelectedItem();
        if (section == null || instructor == null) {
            JOptionPane.showMessageDialog(this, "Please select both section and instructor.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            adminService.assignInstructor(section.getSectionId(), instructor.getUserId());
            JOptionPane.showMessageDialog(this, "Instructor assigned successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

