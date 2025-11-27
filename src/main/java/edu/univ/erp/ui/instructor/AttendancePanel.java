package edu.univ.erp.ui.instructor;

import edu.univ.erp.api.instructor.InstructorAPI;
import edu.univ.erp.api.types.AttendanceRow;
import edu.univ.erp.api.types.GradebookRow;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.api.instructor.InstructorAPI;
import edu.univ.erp.api.types.AttendanceRow;
import edu.univ.erp.api.types.GradebookRow;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AttendancePanel extends JPanel {
    private final InstructorAPI instructorAPI;
    private final String instructorId;
    private JComboBox<SectionRow> sectionComboBox;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    public AttendancePanel(InstructorAPI instructorAPI, String instructorId) {
        this.instructorAPI = instructorAPI;
        this.instructorId = instructorId;

        setLayout(new MigLayout("fill, insets 30", "[grow]", "[]20[grow]"));
        setBackground(UIStyle.BACKGROUND_DARK);

        // Header
        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        headerPanel.setOpaque(false);

        JLabel titleLabel = UIStyle.createHeading("Attendance Management", 1);
        headerPanel.add(titleLabel, "growx");

        add(headerPanel, "growx, wrap");

        // Controls
        JPanel controlsPanel = UIStyle.createCard();
        controlsPanel.setLayout(new MigLayout("insets 20", "[]10[]20[]10[]", "[]"));

        controlsPanel.add(UIStyle.createBodyLabel("Select Section:"));
        sectionComboBox = new JComboBox<>();
        controlsPanel.add(sectionComboBox, "w 200!");

        controlsPanel.add(UIStyle.createBodyLabel("Date (YYYY-MM-DD):"));
        JTextField dateField = UIStyle.createTextField();
        dateField.setText(LocalDate.now().toString());
        controlsPanel.add(dateField, "w 150!");

        JButton loadButton = UIStyle.createPrimaryButton("Load Students");
        loadButton
                .addActionListener(
                        e -> loadStudents((SectionRow) sectionComboBox.getSelectedItem(), dateField.getText()));
        controlsPanel.add(loadButton);

        add(controlsPanel, "growx, wrap");

        // Table
        String[] columns = { "Enrollment ID", "Student ID", "Name", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only Status is editable
            }
        };

        attendanceTable = new JTable(tableModel);
        UIStyle.styleTable(attendanceTable);

        // Add ComboBox editor for Status column
        JComboBox<String> statusCombo = new JComboBox<>(new String[] { "PRESENT", "ABSENT", "LATE", "EXCUSED" });
        attendanceTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(statusCombo));

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        UIStyle.styleScrollPane(scrollPane);
        add(scrollPane, "grow, wrap");

        // Actions
        JPanel actionPanel = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        actionPanel.setOpaque(false);

        JButton saveButton = UIStyle.createPrimaryButton("💾 Save Attendance");
        saveButton.addActionListener(e -> saveAttendance(dateField.getText()));
        actionPanel.add(saveButton, "skip, w 200!");

        add(actionPanel, "growx");

        loadSections();
    }

    private void loadSections() {
        var response = instructorAPI.getSections(Integer.parseInt(instructorId));
        if (response.isSuccess()) {
            List<SectionRow> sections = response.getData();
            for (SectionRow section : sections) {
                sectionComboBox.addItem(section);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Error loading sections: " + response.getMessage());
        }
    }

    private void loadStudents(SectionRow section, String dateStr) {
        if (section == null)
            return;

        try {
            LocalDate date = LocalDate.parse(dateStr);
            tableModel.setRowCount(0);

            var gbResponse = instructorAPI.getGradebook(Integer.parseInt(instructorId), section.sectionId());
            if (!gbResponse.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Error loading students: " + gbResponse.getMessage());
                return;
            }

            List<GradebookRow> rows = gbResponse.getData();

            for (GradebookRow row : rows) {
                // Check if attendance exists
                var attResponse = instructorAPI.getAttendance(row.enrollmentId(), date);
                String status = "PRESENT"; // Default
                if (attResponse.isSuccess() && attResponse.getData() != null) {
                    status = attResponse.getData().status();
                }

                tableModel.addRow(new Object[] {
                        row.enrollmentId(),
                        row.studentId(),
                        row.studentName(),
                        status
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
        }
    }

    private void saveAttendance(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            int rows = tableModel.getRowCount();
            for (int i = 0; i < rows; i++) {
                int enrollmentId = (int) tableModel.getValueAt(i, 0);
                String status = (String) tableModel.getValueAt(i, 3);

                // We need attendanceId if updating, but API markAttendance might handle upsert
                // based on enrollmentId+date?
                // The API markAttendance takes AttendanceRow which has attendanceId.
                // If it's new, attendanceId might be 0.
                // Let's check if we need to fetch existing to get ID or if DAO handles it.
                // AttendanceDao.upsert uses enrollmentId and date to find existing or insert.
                // So attendanceId 0 is fine if we are just passing data.
                // But wait, AttendanceRow is a record.

                AttendanceRow attRow = new AttendanceRow(0, enrollmentId, date, status);
                instructorAPI.markAttendance(attRow);
            }
            JOptionPane.showMessageDialog(this, "Attendance saved successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + e.getMessage());
        }
    }
}
