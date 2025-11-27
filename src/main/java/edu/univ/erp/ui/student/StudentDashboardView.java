package edu.univ.erp.ui.student;

import edu.univ.erp.service.StudentService;
import edu.univ.erp.ui.components.DashboardPanel;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Student dashboard view with statistics and overview metrics
 */
class StudentDashboardView extends DashboardPanel {
    private final StudentService studentService;
    private final String studentId;
    private JLabel enrolledCoursesLabel;
    private JLabel totalCreditsLabel;
    private JLabel averageGradeLabel;
    private JLabel upcomingDeadlinesLabel;
    private JLabel activeRegistrationsLabel;
    private JLabel completedCoursesLabel;

    public StudentDashboardView(StudentService studentService, String studentId) {
        this.studentService = studentService;
        this.studentId = studentId;
        initializeDashboard();
        refreshStats();
    }

    private void initializeDashboard() {
        clearContent();

        // Welcome header
        addSectionHeader("Student Dashboard", "Your academic overview and statistics");

        // Statistics cards in a grid
        JPanel statsGrid = new JPanel(new MigLayout("fillx, wrap, insets 0",
                "[grow 0][grow 0][grow 0]", "[]16[]"));
        statsGrid.setOpaque(false);

        // Row 1: Enrolled Courses, Total Credits, Average Grade
        JPanel enrolledCard = createStatCard("Enrolled Courses", "0", "📚", UIStyle.ACCENT_BLUE);
        JPanel creditsCard = createStatCard("Total Credits", "0", "🎓", UIStyle.SUCCESS_GREEN);
        JPanel gradeCard = createStatCard("Average Grade", "N/A", "⭐", UIStyle.WARNING_ORANGE);

        statsGrid.add(enrolledCard, "growx");
        statsGrid.add(creditsCard, "growx");
        statsGrid.add(gradeCard, "growx");

        // Row 2: Active Registrations, Completed Courses, Upcoming Deadlines
        JPanel registrationsCard = createStatCard("Active Registrations", "0", "📝", UIStyle.ACCENT_BLUE_HOVER);
        JPanel completedCard = createStatCard("Completed Courses", "0", "✅", UIStyle.SUCCESS_GREEN);
        JPanel deadlinesCard = createStatCard("Upcoming Deadlines", "0", "📅", UIStyle.INFO_BLUE);

        statsGrid.add(registrationsCard, "growx");
        statsGrid.add(completedCard, "growx");
        statsGrid.add(deadlinesCard, "growx");

        contentPanel.add(statsGrid, "growx, wrap, gapbottom 24");

        // Quick actions card
        JPanel actionsCard = UIStyle.createCard();
        actionsCard.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]16[]12[]12[]12[]"));

        JLabel actionsTitle = UIStyle.createHeading("Quick Actions", 3);
        actionsCard.add(actionsTitle, "growx, wrap");

        JLabel actionsDesc = UIStyle.createBodyLabel("Navigate to specific sections using the sidebar menu:");
        actionsCard.add(actionsDesc, "growx, wrap");

        addActionItem(actionsCard, "📚 Course Catalog", "Browse and register for available courses", () -> {
        });
        addActionItem(actionsCard, "📝 My Registrations", "View and manage your enrolled courses", () -> {
        });
        addActionItem(actionsCard, "📅 Timetable", "View your class schedule", () -> {
        });
        addActionItem(actionsCard, "📊 Grades", "Check your grades and academic performance", () -> {
        });
        addActionItem(actionsCard, "🔒 Change Password", "Update your account password",
                this::showChangePasswordDialog);

        contentPanel.add(actionsCard, "growx, wrap");

        // Academic summary card
        JPanel summaryCard = UIStyle.createCard();
        summaryCard.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]16[]12[]"));

        JLabel summaryTitle = UIStyle.createHeading("Academic Summary", 3);
        summaryCard.add(summaryTitle, "growx, wrap");

        JLabel summaryDesc = UIStyle
                .createBodyLabel("Keep track of your academic progress and stay on top of your courses.");
        summaryCard.add(summaryDesc, "growx");

        contentPanel.add(summaryCard, "growx");
    }

    private JPanel createStatCard(String title, String value, String icon, Color accentColor) {
        JPanel card = UIStyle.createCard();
        card.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]12[]"));
        card.setPreferredSize(new Dimension(300, 160));

        // Icon and title
        JPanel header = new JPanel(new MigLayout("fillx, insets 0", "[][]push", "[]"));
        header.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font(UIStyle.FONT_FAMILY, Font.PLAIN, 28));
        header.add(iconLabel, "growx 0, gapright 12");

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIStyle.FONT_BODY_BOLD);
        titleLabel.setForeground(UIStyle.TEXT_SECONDARY);
        header.add(titleLabel, "growx");

        card.add(header, "growx, wrap");

        // Value label (will be updated)
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(UIStyle.FONT_H2);
        valueLabel.setForeground(accentColor);
        card.add(valueLabel, "growx");

        // Store reference for updates
        if (title.equals("Enrolled Courses")) {
            enrolledCoursesLabel = valueLabel;
        } else if (title.equals("Total Credits")) {
            totalCreditsLabel = valueLabel;
        } else if (title.equals("Average Grade")) {
            averageGradeLabel = valueLabel;
        } else if (title.equals("Active Registrations")) {
            activeRegistrationsLabel = valueLabel;
        } else if (title.equals("Completed Courses")) {
            completedCoursesLabel = valueLabel;
        } else if (title.equals("Upcoming Deadlines")) {
            upcomingDeadlinesLabel = valueLabel;
        }

        return card;
    }

    private void addActionItem(JPanel parent, String title, String description, Runnable action) {
        JPanel item = new JPanel(new MigLayout("fillx, insets 0", "[][]push", "[]"));
        item.setOpaque(false);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                action.run();
            }
        });

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIStyle.FONT_BODY_BOLD);
        titleLabel.setForeground(UIStyle.TEXT_PRIMARY);
        item.add(titleLabel, "growx 0, gapright 16");

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(UIStyle.FONT_SMALL);
        descLabel.setForeground(UIStyle.TEXT_SECONDARY);
        item.add(descLabel, "growx");

        parent.add(item, "growx, wrap");
    }

    private void showChangePasswordDialog() {
        try {
            String username = studentService.getStudentUsername(Integer.parseInt(studentId));
            edu.univ.erp.ui.common.ChangePasswordDialog dialog = new edu.univ.erp.ui.common.ChangePasswordDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    new edu.univ.erp.service.AuthService(),
                    username);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening password dialog: " + e.getMessage());
        }
    }

    public void refreshStats() {
        try {
            // Get current registrations
            var registrations = studentService.getCurrentRegistrations(Integer.parseInt(studentId));
            int activeRegistrations = registrations.size();

            // Calculate enrolled courses (unique courses)
            long enrolledCourses = registrations.stream()
                    .map(reg -> reg.getCourseCode())
                    .distinct()
                    .count();

            // Calculate total credits
            final int[] totalCreditsArray = { 0 };
            try {
                var catalog = studentService.getCourseCatalog();
                for (var reg : registrations) {
                    final String courseCode = reg.getCourseCode();
                    catalog.stream()
                            .filter(c -> c.getCourseCode().equals(courseCode))
                            .findFirst()
                            .ifPresent(c -> totalCreditsArray[0] += c.getCredits());
                }
            } catch (Exception e) {
                // Ignore
            }
            int totalCredits = totalCreditsArray[0];

            // Calculate average grade
            var grades = studentService.getGrades(Integer.parseInt(studentId));
            final double[] averageGradeArray = { 0 };
            final int[] gradeCountArray = { 0 };
            for (var grade : grades) {
                if (grade.getFinalScore() != null) {
                    averageGradeArray[0] += grade.getFinalScore();
                    gradeCountArray[0]++;
                }
            }
            int gradeCount = gradeCountArray[0];
            double averageGrade = averageGradeArray[0];
            String avgGradeStr = gradeCount > 0 ? String.format("%.1f%%", averageGrade / gradeCount) : "N/A";

            // Completed courses (enrollments with grades)
            int completedCourses = (int) registrations.stream()
                    .filter(reg -> grades.stream()
                            .anyMatch(g -> g.getSectionId() == reg.getSectionId() && g.getFinalScore() != null))
                    .count();

            // Update labels
            if (enrolledCoursesLabel != null)
                enrolledCoursesLabel.setText(String.valueOf(enrolledCourses));
            if (totalCreditsLabel != null)
                totalCreditsLabel.setText(String.valueOf(totalCredits));
            if (averageGradeLabel != null) {
                averageGradeLabel.setText(avgGradeStr);
                if (gradeCount > 0) {
                    double avg = averageGrade / gradeCount;
                    if (avg >= 90) {
                        averageGradeLabel.setForeground(UIStyle.SUCCESS_GREEN);
                    } else if (avg >= 75) {
                        averageGradeLabel.setForeground(UIStyle.ACCENT_BLUE);
                    } else if (avg >= 60) {
                        averageGradeLabel.setForeground(UIStyle.WARNING_ORANGE);
                    } else {
                        averageGradeLabel.setForeground(UIStyle.ERROR_RED);
                    }
                }
            }
            if (activeRegistrationsLabel != null)
                activeRegistrationsLabel.setText(String.valueOf(activeRegistrations));
            if (completedCoursesLabel != null)
                completedCoursesLabel.setText(String.valueOf(completedCourses));
            if (upcomingDeadlinesLabel != null)
                upcomingDeadlinesLabel.setText("0"); // Placeholder

        } catch (SQLException | edu.univ.erp.service.ServiceException ex) {
            System.err.println("Error loading dashboard stats: " + ex.getMessage());
        }
    }
}
