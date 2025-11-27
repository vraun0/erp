package com.sis.app.ui.instructor;

import com.sis.app.model.Section;
import com.sis.app.model.view.GradebookRow;
import com.sis.app.service.ExportService;
import com.sis.app.service.InstructorService;
import com.sis.app.service.ServiceException;
import com.sis.app.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class GradebookPanel extends JPanel {
    private final InstructorService instructorService;
    private final String instructorId;
    private final ExportService exportService = new ExportService();

    private final JComboBox<Section> sectionCombo;
    private final GradebookTableModel tableModel;
    private final JLabel statusLabel;

    GradebookPanel(InstructorService instructorService, String instructorId) {
        this.instructorService = instructorService;
        this.instructorId = instructorId;
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_DARK);

        // Header panel
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 24 28 20 28", "[][grow][]", "[]"));
        headerPanel.setOpaque(false);

        JLabel sectionLabel = new JLabel("Section:");
        sectionLabel.setFont(UIStyle.FONT_BODY_BOLD);
        sectionLabel.setForeground(UIStyle.TEXT_PRIMARY);
        headerPanel.add(sectionLabel, "growx 0, gapright 12");

        sectionCombo = createStyledCombo();
        sectionCombo.setPreferredSize(new Dimension(320, 46));
        sectionCombo.addActionListener(e -> loadSelectedSection());
        headerPanel.add(sectionCombo, "width 320:400:");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyle.FONT_BODY);
        statusLabel.setForeground(UIStyle.TEXT_SECONDARY);
        headerPanel.add(statusLabel, "align right, gapleft 16");
        add(headerPanel, BorderLayout.NORTH);

        // Table with styling
        tableModel = new GradebookTableModel();
        JTable table = new JTable(tableModel);
        UIStyle.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        UIStyle.styleScrollPane(scrollPane);
        scrollPane.getViewport().setBackground(UIStyle.CARD_BACKGROUND);
        add(scrollPane, BorderLayout.CENTER);

        // Footer with buttons
        JPanel footer = new JPanel(new MigLayout("fillx, insets 24 28 24 28", "[grow][right][right]", "[]"));
        footer.setOpaque(false);

        JButton saveButton = UIStyle.createPrimaryButton("💾 Save Grades");
        saveButton.setPreferredSize(new Dimension(180, 46));
        saveButton.addActionListener(e -> saveGrades());

        JButton exportButton = UIStyle.createSecondaryButton("📥 Export CSV");
        exportButton.setPreferredSize(new Dimension(160, 46));
        exportButton.addActionListener(e -> exportGrades());

        footer.add(new JLabel(), "growx");
        footer.add(saveButton, "gapright 12");
        footer.add(exportButton);
        add(footer, BorderLayout.SOUTH);
    }

    private <T> JComboBox<T> createStyledCombo() {
        JComboBox<T> combo = new JComboBox<>();
        combo.setFont(UIStyle.FONT_BODY);
        combo.setBackground(UIStyle.CARD_BACKGROUND);
        combo.setForeground(UIStyle.TEXT_PRIMARY);
        combo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(UIStyle.BORDER_COLOR, 1),
                javax.swing.BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        return combo;
    }

    void refreshSections() {
        try {
            List<Section> sections = instructorService.getSectionsForInstructor(instructorId);
            sectionCombo.removeAllItems();
            for (Section section : sections) {
                sectionCombo.addItem(section);
            }
            if (sectionCombo.getItemCount() > 0) {
                sectionCombo.setSelectedIndex(0);
                loadSelectedSection();
            } else {
                tableModel.setRows(List.of());
                statusLabel.setText("No sections assigned.");
            }
        } catch (SQLException ex) {
            statusLabel.setText("Failed to load sections: " + ex.getMessage());
        }
    }

    private void loadSelectedSection() {
        Section section = (Section) sectionCombo.getSelectedItem();
        if (section == null) {
            tableModel.setRows(List.of());
            return;
        }
        try {
            List<GradebookRow> rows = instructorService.getGradebookForSection(section.getSectionId());
            tableModel.setRows(rows);
            statusLabel.setText(rows.isEmpty() ? "No students enrolled." : " ");
        } catch (SQLException ex) {
            statusLabel.setText("Failed to load gradebook: " + ex.getMessage());
        }
    }

    private void saveGrades() {
        System.out.println("[DEBUG] saveGrades() called");
        Section section = (Section) sectionCombo.getSelectedItem();
        if (section == null) {
            System.out.println("[DEBUG] saveGrades: No section selected, returning");
            return;
        }
        System.out.println("[DEBUG] saveGrades: Section selected: " + section.getSectionId());
        try {
            List<GradebookRow> updated = tableModel.buildUpdatedRows();
            System.out.println("[DEBUG] saveGrades: Built " + updated.size() + " updated rows");
            if (updated.isEmpty()) {
                System.out.println("[DEBUG] saveGrades: No rows to update");
                JOptionPane.showMessageDialog(this, "No grade changes to save.",
                        "No Changes", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            System.out.println("[DEBUG] saveGrades: Calling instructorService.updateGrades()");
            instructorService.updateGrades(section.getSectionId(), updated);
            System.out.println("[DEBUG] saveGrades: updateGrades() completed successfully");
            JOptionPane.showMessageDialog(this, "Grades updated successfully.",
                    "Grades Saved", JOptionPane.INFORMATION_MESSAGE);
            loadSelectedSection();
        } catch (ServiceException ex) {
            System.err.println("[ERROR] saveGrades: ServiceException - " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            System.err.println("[ERROR] saveGrades: SQLException - " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            System.err.println("[ERROR] saveGrades: NumberFormatException - " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Please enter valid numeric scores between 0 and 100.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            System.err.println("[ERROR] saveGrades: Unexpected exception - " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage(),
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportGrades() {
        Section section = (Section) sectionCombo.getSelectedItem();
        if (section == null) {
            return;
        }
        try {
            List<GradebookRow> rows = instructorService.getGradebookForSection(section.getSectionId());
            if (rows.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No grades to export.",
                        "Export", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
            chooser.setDialogTitle("Save Gradebook CSV");
            if (chooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                var file = chooser.getSelectedFile();
                exportService.exportGradebook(rows, file);
                JOptionPane.showMessageDialog(this, "Gradebook exported to " + file.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class GradebookTableModel extends DefaultTableModel {
        private final List<GradebookRow> rows = new ArrayList<>();

        GradebookTableModel() {
            super(new Object[] { "Student", "Roll No", "Midterm", "Final", "Project", "Final Score" }, 0);
        }

        void setRows(List<GradebookRow> data) {
            rows.clear();
            rows.addAll(data);
            setRowCount(0);
            for (GradebookRow row : rows) {
                addRow(new Object[] {
                        row.getStudentId(),
                        row.getRollNumber(),
                        row.getMidtermScore(),
                        row.getFinalExamScore(),
                        row.getProjectScore(),
                        row.getFinalScore()
                });
            }
        }

        List<GradebookRow> buildUpdatedRows() {
            List<GradebookRow> updates = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                GradebookRow original = rows.get(i);
                Integer midterm = parseScore(getValueAt(i, 2));
                Integer fin = parseScore(getValueAt(i, 3));
                Integer project = parseScore(getValueAt(i, 4));
                Integer finalScore = parseScore(getValueAt(i, 5));
                updates.add(new GradebookRow(
                        original.getEnrollmentId(),
                        original.getStudentId(),
                        original.getRollNumber(),
                        midterm,
                        fin,
                        project,
                        finalScore));
            }
            return updates;
        }

        private Integer parseScore(Object value) {
            if (value == null || value.toString().isBlank()) {
                return null;
            }
            try {
                int score = Integer.parseInt(value.toString().trim());
                if (score < 0 || score > 100) {
                    throw new NumberFormatException("Score must be between 0 and 100");
                }
                return score;
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Invalid score format: " + value);
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column >= 2 && column <= 5; // allow editing scores
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 1, 2, 3, 4, 5 -> Integer.class;
                default -> String.class;
            };
        }
    }
}
