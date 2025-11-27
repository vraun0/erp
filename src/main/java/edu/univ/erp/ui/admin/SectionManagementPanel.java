package edu.univ.erp.ui.admin;

import edu.univ.erp.api.admin.AdminAPI;
import edu.univ.erp.api.types.CourseRow;
import edu.univ.erp.api.types.InstructorRow;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class SectionManagementPanel extends JPanel {
    private final AdminAPI adminAPI;

    private final JComboBox<CourseRow> courseCombo = createStyledCombo();
    private final JComboBox<InstructorRow> instructorCombo = createStyledCombo();
    private final JTextField dayTimeField = UIStyle.createTextField();
    private final JTextField roomField = UIStyle.createTextField();
    private final JTextField capacityField = UIStyle.createTextField();
    private final JTextField semesterField = UIStyle.createTextField();
    private final JTextField yearField = UIStyle.createTextField();
    private final JTextField dropDeadlineField = UIStyle.createTextField();

    private final JComboBox<SectionRow> sectionCombo = createStyledCombo();
    private final JComboBox<InstructorRow> assignInstructorCombo = createStyledCombo();

    SectionManagementPanel(AdminAPI adminAPI) {
        this.adminAPI = adminAPI;
        setLayout(new MigLayout("fillx, wrap, insets 24", "[grow]", "[]24[]"));
        setBackground(UIStyle.BACKGROUND_DARK);

        // Create Section Card with scrollable content
        JPanel createSectionCard = UIStyle.createCard();
        createSectionCard.setLayout(new BorderLayout());

        JPanel createSectionContent = new JPanel(new MigLayout("fillx, wrap, insets 0", "[grow]",
                "[]12[]10[]10[]10[]10[]10[]10[]10[]10[]28[]"));
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
        addFormField(createSectionContent, "⏰ Drop Deadline (YYYY-MM-DD)", dropDeadlineField);

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
        assignContent.add(assignButton, "growx, split 2");

        JButton deleteButton = UIStyle.createSecondaryButton("Delete Section");
        deleteButton.setPreferredSize(new Dimension(0, 48));
        deleteButton.addActionListener(e -> deleteSection());
        assignContent.add(deleteButton, "growx");

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
        var coursesRes = adminAPI.getAllCourses();
        if (coursesRes.isSuccess()) {
            courseCombo.setModel(new DefaultComboBoxModel<>(coursesRes.getData().toArray(new CourseRow[0])));
        }

        var instructorsRes = adminAPI.getAllInstructors();
        if (instructorsRes.isSuccess()) {
            instructorCombo
                    .setModel(new DefaultComboBoxModel<>(instructorsRes.getData().toArray(new InstructorRow[0])));
            assignInstructorCombo
                    .setModel(new DefaultComboBoxModel<>(instructorsRes.getData().toArray(new InstructorRow[0])));
        }

        var sectionsRes = adminAPI.getAllSections();
        if (sectionsRes.isSuccess()) {
            sectionCombo.setModel(new DefaultComboBoxModel<>(sectionsRes.getData().toArray(new SectionRow[0])));
        }
    }

    private void createSection() {
        CourseRow course = (CourseRow) courseCombo.getSelectedItem();
        InstructorRow instructor = (InstructorRow) instructorCombo.getSelectedItem();
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
            String dropDeadlineStr = dropDeadlineField.getText().trim();

            if (dayTime.isEmpty() || room.isEmpty() || capacityStr.isEmpty() || semesterStr.isEmpty()
                    || yearStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required (except Drop Deadline).",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int capacity;
            try {
                capacity = Integer.parseInt(capacityStr);
                if (capacity <= 0 || capacity > 500) {
                    JOptionPane.showMessageDialog(this, "Capacity must be between 1 and 500.", "Validation Error",
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

            java.time.LocalDate dropDeadline = null;
            if (!dropDeadlineStr.isEmpty()) {
                try {
                    dropDeadline = java.time.LocalDate.parse(dropDeadlineStr);
                } catch (java.time.format.DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this, "Invalid Drop Deadline format (YYYY-MM-DD).",
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            var response = adminAPI.createSection(
                    course.code(),
                    instructor.instructorId(),
                    dayTime,
                    room,
                    capacity,
                    semester,
                    year,
                    dropDeadline);

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Section created successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dayTimeField.setText("");
                roomField.setText("");
                capacityField.setText("");
                semesterField.setText("");
                yearField.setText("");
                dropDeadlineField.setText("");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format.", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void assignInstructor() {
        SectionRow section = (SectionRow) sectionCombo.getSelectedItem();
        InstructorRow instructor = (InstructorRow) assignInstructorCombo.getSelectedItem();
        if (section == null || instructor == null) {
            JOptionPane.showMessageDialog(this, "Please select both section and instructor.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        var response = adminAPI.assignInstructor(section.sectionId(), instructor.instructorId());
        if (response.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Instructor assigned successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSection() {
        SectionRow section = (SectionRow) sectionCombo.getSelectedItem();
        if (section == null) {
            JOptionPane.showMessageDialog(this, "Please select a section to delete.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete section " + section.sectionId()
                        + "?\nThis action cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            var response = adminAPI.deleteSection(section.sectionId());
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Section deleted successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Deletion Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
