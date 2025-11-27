package edu.univ.erp.ui.student;

import edu.univ.erp.model.view.GradeView;
import edu.univ.erp.service.ExportService;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GradesPanel extends AbstractStudentPanel {
    private final JPanel cardsContainer;
    private final JLabel statusLabel;
    private final JScrollPane scrollPane;
    private final ExportService exportService = new ExportService();

    public GradesPanel(StudentService studentService, String studentId) {
        super(studentService, studentId);
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_DARK);

        // Header with export button
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 24 28 20 28", "[grow][]", "[]"));
        headerPanel.setOpaque(false);
        JLabel headerLabel = UIStyle.createHeading("My Grades", 2);
        headerPanel.add(headerLabel, "growx");

        JButton exportButton = UIStyle.createSecondaryButton("📥 Export CSV");
        exportButton.setPreferredSize(new Dimension(160, 42));
        exportButton.addActionListener(e -> exportGrades());
        headerPanel.add(exportButton, "align right, split 2");

        JButton exportPdfButton = UIStyle.createSecondaryButton("📄 Export PDF");
        exportPdfButton.setPreferredSize(new Dimension(160, 42));
        exportPdfButton.addActionListener(e -> exportGradesPdf());
        headerPanel.add(exportPdfButton, "align right");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyle.FONT_BODY);
        statusLabel.setForeground(UIStyle.TEXT_SECONDARY);
        headerPanel.add(statusLabel, "span 2, wrap, gaptop 8");

        add(headerPanel, BorderLayout.NORTH);

        // Cards container
        cardsContainer = new JPanel();
        cardsContainer.setLayout(new MigLayout("fillx, wrap, insets 24", "[grow]", "[]24[]"));
        cardsContainer.setOpaque(false);
        cardsContainer.setBorder(new EmptyBorder(0, 28, 28, 28));

        scrollPane = new JScrollPane(cardsContainer);
        UIStyle.styleScrollPane(scrollPane);
        scrollPane.getViewport().setBackground(UIStyle.BACKGROUND_DARK);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createGradeCard(String courseDisplay, int sectionId, List<GradeView> grades) {
        JPanel card = UIStyle.createCard();
        card.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]12[]8[]6[]6[]6[]16[]"));
        card.setPreferredSize(new Dimension(0, 320));

        // Course title
        JLabel courseLabel = new JLabel("<html>" + courseDisplay + "</html>");
        courseLabel.setFont(UIStyle.FONT_H3);
        courseLabel.setForeground(UIStyle.ACCENT_BLUE);
        courseLabel.setVerticalAlignment(SwingConstants.TOP);
        card.add(courseLabel, "growx, wrap");

        JLabel sectionLabel = new JLabel("Section #" + sectionId);
        sectionLabel.setFont(UIStyle.FONT_BODY);
        sectionLabel.setForeground(UIStyle.TEXT_SECONDARY);
        card.add(sectionLabel, "growx, wrap");

        // Grades
        int totalScore = 0;
        boolean hasFinal = false;
        for (GradeView grade : grades) {
            String componentName = describeComponent(grade.getComponent());
            addDetailRow(card, componentName, String.valueOf(grade.getScore()) + "%");

            if (grade.getFinalScore() != null) {
                totalScore = grade.getFinalScore();
                hasFinal = true;
            }
        }

        // Final score
        if (hasFinal) {
            JPanel finalPanel = new JPanel(new MigLayout("fillx, insets 8 0 0 0", "[grow]", "[]"));
            finalPanel.setOpaque(false);
            JLabel finalLabel = new JLabel("Final Grade");
            finalLabel.setFont(UIStyle.FONT_BODY_BOLD);
            finalLabel.setForeground(UIStyle.TEXT_PRIMARY);
            finalPanel.add(finalLabel, "growx 0");

            JLabel finalScoreLabel = new JLabel(String.valueOf(totalScore) + "%");
            finalScoreLabel.setFont(new Font(UIStyle.FONT_FAMILY, Font.BOLD, 20));
            finalScoreLabel.setForeground(getGradeColor(totalScore));
            finalPanel.add(finalScoreLabel, "growx, align right");
            card.add(finalPanel, "growx");
        }

        return card;
    }

    private Color getGradeColor(int score) {
        if (score >= 90)
            return UIStyle.SUCCESS_GREEN;
        if (score >= 75)
            return UIStyle.ACCENT_BLUE;
        if (score >= 60)
            return UIStyle.WARNING_ORANGE;
        return UIStyle.ERROR_RED;
    }

    private void addDetailRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new MigLayout("fillx, insets 0", "[][grow]", "[]"));
        row.setOpaque(false);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(UIStyle.FONT_BODY);
        labelComp.setForeground(UIStyle.TEXT_SECONDARY);
        row.add(labelComp, "growx 0, width 160:160:");

        JLabel valueComp = new JLabel("<html>" + value + "</html>");
        valueComp.setFont(UIStyle.FONT_BODY_BOLD);
        valueComp.setForeground(UIStyle.TEXT_PRIMARY);
        valueComp.setHorizontalAlignment(SwingConstants.RIGHT);
        valueComp.setVerticalAlignment(SwingConstants.CENTER);
        row.add(valueComp, "growx");

        parent.add(row, "growx, wrap");
    }

    @Override
    public void refreshData() {
        try {
            List<GradeView> grades = studentService.getGrades(Integer.parseInt(studentId));

            cardsContainer.removeAll();

            // Group grades by course and section
            Map<String, Map<Integer, List<GradeView>>> grouped = grades.stream()
                    .collect(Collectors.groupingBy(
                            GradeView::getCourseDisplay,
                            Collectors.groupingBy(GradeView::getSectionId)));

            for (Map.Entry<String, Map<Integer, List<GradeView>>> courseEntry : grouped.entrySet()) {
                for (Map.Entry<Integer, List<GradeView>> sectionEntry : courseEntry.getValue().entrySet()) {
                    cardsContainer.add(createGradeCard(courseEntry.getKey(),
                            sectionEntry.getKey(), sectionEntry.getValue()), "growx, wrap");
                }
            }

            statusLabel.setText(grades.isEmpty() ? "No grades available yet"
                    : grouped.size() + " course" + (grouped.size() != 1 ? "s" : "") + " with grades");

            cardsContainer.revalidate();
            cardsContainer.repaint();
            scrollPane.revalidate();

        } catch (SQLException | ServiceException ex) {
            statusLabel.setText("Failed to load grades: " + ex.getMessage());
            statusLabel.setForeground(UIStyle.ERROR_RED);
        }
    }

    private String describeComponent(int component) {
        return switch (component) {
            case 1 -> "📝 Midterm Exam";
            case 2 -> "📄 Final Exam";
            case 3 -> "💼 Project";
            case 4 -> "📋 Quiz";
            default -> "Component " + component;
        };
    }

    private void exportGrades() {
        try {
            List<GradeView> grades = studentService.getGrades(Integer.parseInt(studentId));
            if (grades.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No grades to export.",
                        "Export", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Grades CSV");
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                var file = chooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new java.io.File(file.getAbsolutePath() + ".csv");
                }
                exportService.exportStudentGrades(grades, file);
                JOptionPane.showMessageDialog(this, "Grades exported to " + file.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException | ServiceException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportGradesPdf() {
        try {
            List<GradeView> grades = studentService.getGrades(Integer.parseInt(studentId));
            if (grades.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No grades to export.",
                        "Export", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Grades PDF");
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                var file = chooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    file = new java.io.File(file.getAbsolutePath() + ".pdf");
                }
                exportService.exportTranscriptPDF(Integer.parseInt(studentId), grades, file);
                JOptionPane.showMessageDialog(this, "Transcript exported to " + file.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException | ServiceException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
