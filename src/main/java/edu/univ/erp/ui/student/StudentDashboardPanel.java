package edu.univ.erp.ui.student;

import edu.univ.erp.service.StudentService;
import edu.univ.erp.ui.components.DashboardPanel;
import edu.univ.erp.ui.components.StatCard;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Modern student dashboard with summary widgets
 */
public class StudentDashboardPanel extends DashboardPanel {
    private final StudentService studentService;
    private final String studentId;

    private JPanel dashboardHomePanel;
    private StatCard gpaCard;
    private StatCard attendanceCard;
    private StatCard feesCard;

    public StudentDashboardPanel(StudentService studentService, String studentId) {
        this.studentService = studentService;
        this.studentId = studentId;

        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_DARK);

        // Create Dashboard Home Panel
        createDashboardHome();

        add(dashboardHomePanel, BorderLayout.CENTER);
    }

    private void createDashboardHome() {
        dashboardHomePanel = new JPanel(new MigLayout("fill, insets 30", "[grow][grow][grow]", "[]20[]20[grow]"));
        dashboardHomePanel.setBackground(UIStyle.BACKGROUND_DARK);

        // Welcome Message
        JLabel welcomeLabel = UIStyle.createHeading("Welcome back, " + studentId, 1);
        dashboardHomePanel.add(welcomeLabel, "span, wrap");

        // Row 1: Stats Widgets
        gpaCard = new StatCard("GPA", "3.8", "🎓", UIStyle.ACCENT_BLUE);
        attendanceCard = new StatCard("Attendance", "95%", "📅", UIStyle.SUCCESS_GREEN);
        feesCard = new StatCard("Pending Fees", "$0.00", "💰", UIStyle.WARNING_ORANGE);

        dashboardHomePanel.add(gpaCard, "grow");
        dashboardHomePanel.add(attendanceCard, "grow");
        dashboardHomePanel.add(feesCard, "grow, wrap");

        // Row 2: Next Class & Recent Activity

        // Next Class Widget
        JPanel nextClassPanel = UIStyle.createGradientCard(UIStyle.GRADIENT_START, UIStyle.GRADIENT_END);
        nextClassPanel.setLayout(new MigLayout("fillx, insets 20", "[grow]", "[]4[]12[]"));

        JLabel nextClassTitle = new JLabel("Next Class");
        nextClassTitle.setFont(UIStyle.FONT_SMALL);
        nextClassTitle.setForeground(new Color(255, 255, 255, 180));
        nextClassPanel.add(nextClassTitle, "wrap");

        JLabel className = new JLabel("CS101 - Intro to CS");
        className.setFont(UIStyle.FONT_H2);
        className.setForeground(Color.WHITE);
        nextClassPanel.add(className, "wrap");

        JLabel classTime = new JLabel("Today, 10:00 AM • Room 301");
        classTime.setFont(UIStyle.FONT_BODY);
        classTime.setForeground(new Color(255, 255, 255, 220));
        nextClassPanel.add(classTime);

        dashboardHomePanel.add(nextClassPanel, "span 2, grow");

        // Recent Activity / Notifications
        JPanel recentActivity = UIStyle.createCard();
        recentActivity.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]12[]"));

        recentActivity.add(UIStyle.createHeading("Recent Activity", 3), "growx, wrap");

        addActivityItem(recentActivity, "📝 Registered for CS101", "Today, 10:30 AM");
        addActivityItem(recentActivity, "💰 Paid Semester Fees", "Yesterday, 2:15 PM");
        addActivityItem(recentActivity, "📊 Grades Updated: Math 202", "Nov 25, 9:00 AM");

        dashboardHomePanel.add(recentActivity, "grow, wrap");
    }

    private void addActivityItem(JPanel parent, String title, String time) {
        JPanel item = new JPanel(new MigLayout("fillx, insets 12 0", "[grow][]", "[]"));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.BORDER_COLOR));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIStyle.FONT_BODY);
        titleLabel.setForeground(UIStyle.TEXT_PRIMARY);
        item.add(titleLabel, "growx");

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(UIStyle.FONT_SMALL);
        timeLabel.setForeground(UIStyle.TEXT_SECONDARY);
        item.add(timeLabel);

        parent.add(item, "growx");
    }

    public void refreshStats() {
        try {
            // Update GPA
            double gpa = studentService.calculateGPA(Integer.parseInt(studentId));
            gpaCard.setValue(String.format("%.2f", gpa));

            // Update Attendance
            java.util.List<edu.univ.erp.model.Attendance> attendanceList = studentService
                    .getAttendance(Integer.parseInt(studentId));
            if (attendanceList.isEmpty()) {
                attendanceCard.setValue("N/A");
            } else {
                long present = attendanceList.stream().filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())).count();
                int percentage = (int) ((present * 100) / attendanceList.size());
                attendanceCard.setValue(percentage + "%");
            }

            // Update Fees
            java.util.List<edu.univ.erp.model.Fee> fees = studentService.getFees(Integer.parseInt(studentId));
            double pendingAmount = fees.stream()
                    .filter(f -> "PENDING".equalsIgnoreCase(f.getStatus()))
                    .mapToDouble(f -> f.getAmount().doubleValue())
                    .sum();
            feesCard.setValue(String.format("$%.2f", pendingAmount));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // No longer needed as tabs are handled by MainFrame
    public void showTab(String tabName) {
        // No-op or removed
    }

    public void refreshAllTabs() {
        refreshStats();
    }

    public String getStudentId() {
        return studentId;
    }
}
