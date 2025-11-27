package edu.univ.erp.ui.instructor;

import edu.univ.erp.api.instructor.InstructorAPI;
import edu.univ.erp.api.types.GradebookRow;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.service.ExportService;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GradebookPanel extends JPanel {
    private final InstructorAPI instructorAPI;
    private final String instructorId;
    private final ExportService exportService = new ExportService();

    private final JComboBox<SectionRow> sectionCombo;
    private final GradebookTableModel tableModel;
    private final JLabel statusLabel;

    public GradebookPanel(InstructorAPI instructorAPI, String instructorId) {
        this.instructorAPI = instructorAPI;
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
        JPanel footer = new JPanel(new MigLayout("fillx, insets 24 28 24 28", "[grow][right][right][right]", "[]"));
        footer.setOpaque(false);

        JButton saveButton = UIStyle.createPrimaryButton("💾 Save Grades");
        saveButton.setPreferredSize(new Dimension(180, 46));
        saveButton.addActionListener(e -> saveGrades());

        JButton importButton = UIStyle.createSecondaryButton("📥 Import CSV");
        importButton.setPreferredSize(new Dimension(160, 46));
        importButton.addActionListener(e -> importGrades());

        JButton exportButton = UIStyle.createSecondaryButton("📥 Export CSV");
        exportButton.setPreferredSize(new Dimension(160, 46));
        exportButton.addActionListener(e -> exportGrades());

        JButton statsButton = UIStyle.createSecondaryButton("📊 Statistics");
        statsButton.setPreferredSize(new Dimension(160, 46));
        statsButton.addActionListener(e -> showStatistics());

        footer.add(new JLabel(), "growx");
        footer.add(saveButton, "gapright 12");
        footer.add(importButton, "gapright 12");
        footer.add(exportButton, "gapright 12");
        footer.add(statsButton);
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

    public void refreshSections() {
        var response = instructorAPI.getSections(Integer.parseInt(instructorId));
        if (response.isSuccess()) {
            List<SectionRow> sections = response.getData();
            sectionCombo.removeAllItems();
            for (SectionRow section : sections) {
                sectionCombo.addItem(section);
            }
            if (sectionCombo.getItemCount() > 0) {
                sectionCombo.setSelectedIndex(0);
                loadSelectedSection();
            } else {
                tableModel.setRows(List.of());
                statusLabel.setText("No sections assigned.");
            }
        } else {
            statusLabel.setText("Failed to load sections: " + response.getMessage());
        }
    }

    private void loadSelectedSection() {
        SectionRow section = (SectionRow) sectionCombo.getSelectedItem();
        if (section == null) {
            tableModel.setRows(List.of());
            return;
        }
        var response = instructorAPI.getGradebook(Integer.parseInt(instructorId), section.sectionId());
        if (response.isSuccess()) {
            List<GradebookRow> rows = response.getData();
            tableModel.setRows(rows);
            statusLabel.setText(rows.isEmpty() ? "No students enrolled." : " ");
        } else {
            statusLabel.setText("Failed to load gradebook: " + response.getMessage());
        }
    }

    private void saveGrades() {
        System.out.println("[DEBUG] saveGrades() called");
        SectionRow section = (SectionRow) sectionCombo.getSelectedItem();
        if (section == null) {
            System.out.println("[DEBUG] saveGrades: No section selected, returning");
            return;
        }
        System.out.println("[DEBUG] saveGrades: Section selected: " + section.sectionId());
        try {
            List<GradebookRow> updated = tableModel.buildUpdatedRows();
            System.out.println("[DEBUG] saveGrades: Built " + updated.size() + " updated rows");
            if (updated.isEmpty()) {
                System.out.println("[DEBUG] saveGrades: No rows to update");
                JOptionPane.showMessageDialog(this, "No grade changes to save.",
                        "No Changes", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            System.out.println("[DEBUG] saveGrades: Calling instructorAPI.updateGrades()");
            var response = instructorAPI.updateGrades(Integer.parseInt(instructorId), section.sectionId(), updated);
            if (response.isSuccess()) {
                System.out.println("[DEBUG] saveGrades: updateGrades() completed successfully");
                JOptionPane.showMessageDialog(this, "Grades updated successfully.",
                        "Grades Saved", JOptionPane.INFORMATION_MESSAGE);
                loadSelectedSection();
            } else {
                System.err.println("[ERROR] saveGrades: API Error - " + response.getMessage());
                JOptionPane.showMessageDialog(this, response.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
            }
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

    private void importGrades() {
        SectionRow currentSection = (SectionRow) sectionCombo.getSelectedItem();
        if (currentSection == null)
            return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Grades CSV");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                edu.univ.erp.service.ExportService exportService = new edu.univ.erp.service.ExportService();
                java.util.List<edu.univ.erp.model.view.GradebookRow> rows = exportService.importGradebook(file);
                // Convert to API type
                List<GradebookRow> apiRows = rows.stream()
                        .map(r -> new GradebookRow(
                                r.getEnrollmentId(), r.getStudentId(), r.getStudentName(), r.getRollNumber(),
                                r.getMidtermScore(), r.getFinalExamScore(), r.getProjectScore(), r.getFinalScore()))
                        .collect(Collectors.toList());

                // Update grades
                instructorAPI.updateGrades(Integer.parseInt(instructorId), currentSection.sectionId(), apiRows);

                // Refresh view
                loadSelectedSection();
                JOptionPane.showMessageDialog(this, "Grades imported successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error importing grades: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportGrades() {
        SectionRow section = (SectionRow) sectionCombo.getSelectedItem();
        if (section == null) {
            return;
        }
        var response = instructorAPI.getGradebook(Integer.parseInt(instructorId), section.sectionId());
        if (!response.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Failed to fetch grades: " + response.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<GradebookRow> rows = response.getData();
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No grades to export.",
                    "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Map to model.view.GradebookRow for ExportService
        List<edu.univ.erp.model.view.GradebookRow> exportRows = rows.stream()
                .map(r -> new edu.univ.erp.model.view.GradebookRow(
                        r.enrollmentId(), r.studentId(), r.studentName(), r.rollNumber(),
                        r.midtermScore(), r.finalExamScore(), r.projectScore(), r.finalScore()))
                .collect(Collectors.toList());

        try {
            javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
            chooser.setDialogTitle("Save Gradebook CSV");
            if (chooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                var file = chooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new java.io.File(file.getAbsolutePath() + ".csv");
                }
                exportService.exportGradebook(exportRows, file);
                JOptionPane.showMessageDialog(this, "Gradebook exported to " + file.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showStatistics() {
        SectionRow section = (SectionRow) sectionCombo.getSelectedItem();
        if (section == null) {
            return;
        }
        var response = instructorAPI.getClassStatistics(Integer.parseInt(instructorId), section.sectionId());
        if (response.isSuccess()) {
            var stats = response.getData();

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Class Statistics", true);
            dialog.setLayout(new MigLayout("fill, insets 20", "[grow]", "[]12[]"));
            dialog.getContentPane().setBackground(UIStyle.BACKGROUND_DARK);

            JPanel statsPanel = UIStyle.createCard();
            statsPanel.setLayout(new MigLayout("fillx, wrap 2", "[grow][grow]", "[]12[]"));

            addStatRow(statsPanel, "Average Score", String.format("%.2f%%", stats.averageScore()));
            addStatRow(statsPanel, "Highest Score", stats.maxScore() + "%");
            addStatRow(statsPanel, "Lowest Score", stats.minScore() + "%");
            addStatRow(statsPanel, "Standard Deviation", String.format("%.2f", stats.stdDev()));
            addStatRow(statsPanel, "Total Students", String.valueOf(stats.studentCount()));

            dialog.add(statsPanel, "growx, wrap");

            // Distribution chart (simple bars)
            JPanel chartPanel = UIStyle.createCard();
            chartPanel.setLayout(new MigLayout("fillx, wrap", "[grow]", "[]"));
            chartPanel.add(new JLabel("Grade Distribution"), "wrap");

            java.util.Map<String, Integer> dist = stats.gradeDistribution();
            String[] grades = { "A", "B", "C", "D", "F" };
            int maxCount = dist.values().stream().max(Integer::compare).orElse(1);
            if (maxCount == 0)
                maxCount = 1;

            for (String grade : grades) {
                int count = dist.getOrDefault(grade, 0);
                JPanel barContainer = new JPanel(new MigLayout("fillx, insets 0", "[30!][grow][30!]", "[]"));
                barContainer.setOpaque(false);

                JLabel gradeLabel = new JLabel(grade);
                gradeLabel.setForeground(UIStyle.TEXT_PRIMARY);
                barContainer.add(gradeLabel);

                JPanel bar = new JPanel();
                bar.setBackground(UIStyle.ACCENT_BLUE);
                bar.setPreferredSize(new Dimension(0, 20));
                // Calculate width percentage
                int width = (int) ((double) count / maxCount * 200); // Max width 200px approx
                barContainer.add(bar, "height 20!, width " + width + "!");

                JLabel countLabel = new JLabel(String.valueOf(count));
                countLabel.setForeground(UIStyle.TEXT_SECONDARY);
                barContainer.add(countLabel);

                chartPanel.add(barContainer, "growx");
            }
            dialog.add(chartPanel, "growx");

            JButton closeBtn = UIStyle.createPrimaryButton("Close");
            closeBtn.addActionListener(e -> dialog.dispose());
            dialog.add(closeBtn, "align center");

            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } else {
            JOptionPane.showMessageDialog(this, "Error loading statistics: " + response.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addStatRow(JPanel parent, String label, String value) {
        JLabel l = new JLabel(label);
        l.setForeground(UIStyle.TEXT_SECONDARY);
        JLabel v = new JLabel(value);
        v.setFont(UIStyle.FONT_BODY_BOLD);
        v.setForeground(UIStyle.TEXT_PRIMARY);
        parent.add(l);
        parent.add(v, "align right");
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
                        row.studentName(),
                        row.rollNumber(),
                        row.midtermScore(),
                        row.finalExamScore(),
                        row.projectScore(),
                        row.finalScore()
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
                        original.enrollmentId(),
                        original.studentId(),
                        original.studentName(),
                        original.rollNumber(),
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
