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
        setLayout(new MigLayout("fillx, wrap, insets 24", "[grow]", "[]24[]"));
        setBackground(UIStyle.BACKGROUND_DARK);

        // Create Section Card with scrollable content
        JPanel createSectionCard = UIStyle.createCard();
        createSectionCard.setLayout(new BorderLayout());

        JPanel createSectionContent = new JPanel(new MigLayout("fillx, wrap, insets 0", "[grow]",
                "[]12[]10[]10[]10[]10[]10[]10[]10[]28[]"));
        createSectionContent.setOpaque(false);

        JLabel createTitle = UIStyle.createHeading("Create New Section", 3);
        createSectionContent.add(createTitle, "growx, wrap");

        addComboField(createSectionContent, "📚 Course", courseCombo);
        addComboField(createSectionContent, "👨‍🏫 Instructor", instructorCombo);
        addFormField(createSectionContent, "📅 Day & Time", dayTimeField);
        addFormField(createSectionContent, "🏢 Room", roomField);
        addFormField(createSectionContent, "👥 Capacity", capacityField);
        addFormField(createSectionContent, "📆 Semester", semesterField);
        addFormField(createSectionContent, "📅 Year", yearField);

        JButton createSectionButton = UIStyle.createPrimaryButton("Create Section");
        createSectionButton.setPreferredSize(new Dimension(0, 48));
        createSectionButton.addActionListener(e -> createSection());
        createSectionContent.add(createSectionButton, "growx");

        JScrollPane createSectionScroll = new JScrollPane(createSectionContent);
        createSectionScroll.setBorder(null);
        createSectionScroll.setOpaque(false);
        createSectionScroll.getViewport().setOpaque(false);
        createSectionScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        createSectionScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UIStyle.styleScrollPane(createSectionScroll);

        createSectionCard.add(createSectionScroll, BorderLayout.CENTER);
        createSectionCard.setPreferredSize(new Dimension(0, 600));
        add(createSectionCard, "growx, wrap");

        // Assign Instructor Card with scrollable content
        JPanel assignCard = UIStyle.createCard();
        assignCard.setLayout(new BorderLayout());

        JPanel assignContent = new JPanel(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]12[]10[]10[]24[]"));
        assignContent.setOpaque(false);

        JLabel assignTitle = UIStyle.createHeading("Assign Instructor to Section", 3);
        assignContent.add(assignTitle, "growx, wrap");

        addComboField(assignContent, "📖 Section", sectionCombo);
        addComboField(assignContent, "👨‍🏫 Instructor", assignInstructorCombo);

        JButton assignButton = UIStyle.createPrimaryButton("Assign Instructor");
        assignButton.setPreferredSize(new Dimension(0, 48));
        assignButton.addActionListener(e -> assignInstructor());
        assignContent.add(assignButton, "growx");

        JScrollPane assignScroll = new JScrollPane(assignContent);
        assignScroll.setBorder(null);
        assignScroll.setOpaque(false);
        assignScroll.getViewport().setOpaque(false);
        assignScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        assignScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UIStyle.styleScrollPane(assignScroll);

        assignCard.add(assignScroll, BorderLayout.CENTER);
        assignCard.setPreferredSize(new Dimension(0, 320));
        add(assignCard, "growx");
    }

    private <T> JComboBox<T> createStyledCombo() {
        JComboBox<T> combo = new JComboBox<>();
        combo.setFont(UIStyle.FONT_BODY);
        combo.setBackground(UIStyle.CARD_BACKGROUND);
        combo.setForeground(UIStyle.TEXT_PRIMARY);
        combo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(UIStyle.BORDER_COLOR, 1),
                javax.swing.BorderFactory.createEmptyBorder(14, 18, 14, 18)));
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
        parent.add(labelComp, "growx, width 160:160:");
        parent.add(field, "growx, h 48!, wrap");
    }

    private void addComboField(JPanel parent, String label, JComboBox<?> combo) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(UIStyle.FONT_BODY_BOLD);
        labelComp.setForeground(UIStyle.TEXT_PRIMARY);
        parent.add(labelComp, "growx, width 160:160:");
        parent.add(combo, "growx, h 48!, wrap");
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
            String dayTime = dayTimeField.getText().trim();
            String room = roomField.getText().trim();
            String capacityStr = capacityField.getText().trim();
            String semesterStr = semesterField.getText().trim();
            String yearStr = yearField.getText().trim();

            if (dayTime.isEmpty() || room.isEmpty() || capacityStr.isEmpty() || semesterStr.isEmpty()
                    || yearStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int capacity;
            try {
                capacity = Integer.parseInt(capacityStr);
                if (capacity <= 0 || capacity > 200) {
                    JOptionPane.showMessageDialog(this, "Capacity must be between 1 and 200.", "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Capacity must be a valid number.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int semester;
            try {
                semester = Integer.parseInt(semesterStr);
                if (semester < 1 || semester > 3) {
                    JOptionPane.showMessageDialog(this, "Semester must be between 1 and 3.", "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Semester must be a valid number.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int year;
            try {
                year = Integer.parseInt(yearStr);
                int currentYear = java.time.Year.now().getValue();
                if (year < currentYear) {
                    JOptionPane.showMessageDialog(this, "Year cannot be in the past.", "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Year must be a valid number.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            adminService.createSection(
                    course.getCode(),
                    instructor.getUserId(),
                    dayTime,
                    room,
                    capacity,
                    semester,
                    year);
            JOptionPane.showMessageDialog(this, "Section created successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dayTimeField.setText("");
            roomField.setText("");
            capacityField.setText("");
            semesterField.setText("");
            yearField.setText("");
            refreshData();
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
