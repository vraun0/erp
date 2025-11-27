package edu.univ.erp.ui.instructor;

import edu.univ.erp.api.instructor.InstructorAPI;
import edu.univ.erp.ui.components.DashboardPanel;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Instructor dashboard view with statistics and overview metrics
 */
class InstructorDashboardView extends DashboardPanel {
    private final InstructorAPI instructorAPI;
    private final String instructorId;
    private JLabel totalSectionsLabel;
    private JLabel totalStudentsLabel;
    private JLabel totalCoursesLabel;
    private JLabel averageClassSizeLabel;
    private JLabel totalEnrollmentsLabel;
    private JLabel pendingGradesLabel;

    public InstructorDashboardView(InstructorAPI instructorAPI, String instructorId) {
        this.instructorAPI = instructorAPI;
        this.instructorId = instructorId;
        try {
            initializeDashboard();
            refreshStats();
        } catch (Exception ex) {
            System.err.println("Error initializing instructor dashboard: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void initializeDashboard() {
        clearContent();

        // Welcome header
        addSectionHeader("Instructor Dashboard", "Your teaching overview and statistics");

        // Statistics cards in a grid
        JPanel statsGrid = new JPanel(new MigLayout("fillx, wrap, insets 0",
                "[grow 0][grow 0][grow 0]", "[]16[]"));
        statsGrid.setOpaque(false);

        // Row 1: Sections, Students, Courses
        JPanel sectionsCard = createStatCard("Assigned Sections", "0", "📖", UIStyle.ACCENT_BLUE);
        JPanel studentsCard = createStatCard("Total Students", "0", "👥", UIStyle.SUCCESS_GREEN);
        JPanel coursesCard = createStatCard("Courses Teaching", "0", "📚", UIStyle.WARNING_ORANGE);

        statsGrid.add(sectionsCard, "growx");
        statsGrid.add(studentsCard, "growx");
        statsGrid.add(coursesCard, "growx");

        // Row 2: Average Class Size, Total Enrollments, Pending Grades
        JPanel avgSizeCard = createStatCard("Average Class Size", "0", "👨‍🏫", UIStyle.ACCENT_BLUE_HOVER);
        JPanel enrollmentsCard = createStatCard("Total Enrollments", "0", "📝", UIStyle.INFO_BLUE);
        JPanel pendingCard = createStatCard("Sections with Pending Grades", "0", "⏳", UIStyle.WARNING_ORANGE);

        statsGrid.add(avgSizeCard, "growx");
        statsGrid.add(enrollmentsCard, "growx");
        statsGrid.add(pendingCard, "growx");

        contentPanel.add(statsGrid, "growx, wrap, gapbottom 24");

        // Quick actions card
        JPanel actionsCard = UIStyle.createCard();
        actionsCard.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow][grow]", "[]16[]"));

        JLabel actionsTitle = UIStyle.createHeading("Quick Actions", 3);
        actionsCard.add(actionsTitle, "span, growx, wrap");

        addActionItem(actionsCard, "📖 My Sections", "View all assigned course sections", () -> {
        });
        addActionItem(actionsCard, "📋 Gradebook", "Manage grades and student assessments", () -> {
        });
        addActionItem(actionsCard, "🔒 Change Password", "Update your account password",
                this::showChangePasswordDialog);

        contentPanel.add(actionsCard, "growx, wrap");

        // Upcoming Classes card
        JPanel summaryCard = UIStyle.createCard();
        summaryCard.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]16[]"));

        JLabel summaryTitle = UIStyle.createHeading("Upcoming Classes", 3);
        summaryCard.add(summaryTitle, "growx, wrap");

        addClassItem(summaryCard, "CS101 - Intro to CS", "Mon, 10:00 AM", "Room 301", UIStyle.ACCENT_BLUE);
        addClassItem(summaryCard, "CS202 - Data Structures", "Mon, 2:00 PM", "Lab 2", UIStyle.WARNING_ORANGE);
        addClassItem(summaryCard, "CS305 - Algorithms", "Tue, 11:00 AM", "Room 405", UIStyle.SUCCESS_GREEN);

        contentPanel.add(summaryCard, "growx");
    }

    private void addClassItem(JPanel parent, String course, String time, String room, Color accent) {
        JPanel item = new JPanel(new MigLayout("fillx, insets 12 0", "[4px!][grow][right]", "[]"));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.BORDER_COLOR));

        // Color strip
        JPanel strip = new JPanel();
        strip.setBackground(accent);
        item.add(strip, "growy");

        JPanel left = new JPanel(new MigLayout("insets 0", "[]", "[]0[]"));
        left.setOpaque(false);

        JLabel courseLabel = new JLabel(course);
        courseLabel.setFont(UIStyle.FONT_BODY_BOLD);
        courseLabel.setForeground(UIStyle.TEXT_PRIMARY);
        left.add(courseLabel, "wrap");

        JLabel roomLabel = new JLabel(room);
        roomLabel.setFont(UIStyle.FONT_SMALL);
        roomLabel.setForeground(UIStyle.TEXT_SECONDARY);
        left.add(roomLabel);

        item.add(left, "growx");

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(UIStyle.FONT_BODY);
        timeLabel.setForeground(UIStyle.TEXT_PRIMARY);
        item.add(timeLabel);

        parent.add(item, "growx");
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
        if (title.equals("Assigned Sections")) {
            totalSectionsLabel = valueLabel;
        } else if (title.equals("Total Students")) {
            totalStudentsLabel = valueLabel;
        } else if (title.equals("Courses Teaching")) {
            totalCoursesLabel = valueLabel;
        } else if (title.equals("Average Class Size")) {
            averageClassSizeLabel = valueLabel;
        } else if (title.equals("Total Enrollments")) {
            totalEnrollmentsLabel = valueLabel;
        } else if (title.equals("Sections with Pending Grades")) {
            pendingGradesLabel = valueLabel;
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
            var response = instructorAPI.getInstructorName(Integer.parseInt(instructorId));
            String username = response.isSuccess() ? response.getData() : "Unknown";
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
        var response = instructorAPI.getDashboardStats(Integer.parseInt(instructorId));
        if (response.isSuccess()) {
            var stats = response.getData();

            if (totalSectionsLabel != null)
                totalSectionsLabel.setText(String.valueOf(stats.totalSections()));
            if (totalStudentsLabel != null)
                totalStudentsLabel.setText(String.valueOf(stats.totalStudents()));
            if (totalCoursesLabel != null)
                totalCoursesLabel.setText(String.valueOf(stats.totalCourses()));
            if (averageClassSizeLabel != null)
                averageClassSizeLabel.setText(String.format("%.1f", stats.averageClassSize()));
            if (totalEnrollmentsLabel != null)
                totalEnrollmentsLabel.setText(String.valueOf(stats.totalEnrollments()));
            if (pendingGradesLabel != null)
                pendingGradesLabel.setText(String.valueOf(stats.sectionsWithPendingGrades()));
        } else {
            System.err.println("Error loading dashboard stats: " + response.getMessage());
        }
    }
}
