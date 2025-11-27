package edu.univ.erp.ui.admin;

import edu.univ.erp.api.admin.AdminAPI;
import edu.univ.erp.api.maintenance.MaintenanceAPI;
import edu.univ.erp.ui.components.DashboardPanel;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Admin dashboard view with statistics and overview metrics
 */
class AdminDashboardView extends DashboardPanel {
    private final AdminAPI adminAPI;
    private final MaintenanceAPI maintenanceAPI;
    private JLabel totalUsersLabel;
    private JLabel totalStudentsLabel;
    private JLabel totalInstructorsLabel;
    private JLabel totalCoursesLabel;
    private JLabel totalSectionsLabel;
    private JLabel activeEnrollmentsLabel;
    private JLabel maintenanceStatusLabel;

    public AdminDashboardView(AdminAPI adminAPI, MaintenanceAPI maintenanceAPI) {
        this.adminAPI = adminAPI;
        this.maintenanceAPI = maintenanceAPI;
        initializeDashboard();
        refreshStats();
    }

    private void initializeDashboard() {
        clearContent();

        // Welcome header
        addSectionHeader("Admin Dashboard", "System overview and key metrics");

        // Statistics cards in a grid (4 columns)
        JPanel statsGrid = new JPanel(new MigLayout("fillx, wrap, insets 0",
                "[grow 0][grow 0][grow 0][grow 0]", "[]16[]"));
        statsGrid.setOpaque(false);

        // Row 1: Users, Students, Instructors, Courses
        statsGrid.add(createStatCard("Total Users", "0", "👥", UIStyle.ACCENT_BLUE), "growx");
        statsGrid.add(createStatCard("Students", "0", "🎓", UIStyle.SUCCESS_GREEN), "growx");
        statsGrid.add(createStatCard("Instructors", "0", "👨‍🏫", UIStyle.INFO_BLUE), "growx");
        statsGrid.add(createStatCard("Courses", "0", "📚", UIStyle.WARNING_ORANGE), "growx");

        // Row 2: Sections, Enrollments
        statsGrid.add(createStatCard("Sections", "0", "📖", UIStyle.ACCENT_BLUE_HOVER), "growx");
        statsGrid.add(createStatCard("Active Enrollments", "0", "📝", UIStyle.SUCCESS_GREEN), "growx");

        contentPanel.add(statsGrid, "growx, wrap, gapbottom 24");

        // System status card
        JPanel statusCard = UIStyle.createCard();
        statusCard.setLayout(new MigLayout("fillx, insets 0", "[grow][]", "[]"));

        JPanel statusText = new JPanel(new MigLayout("insets 0", "[grow]", "[]4[]"));
        statusText.setOpaque(false);

        JLabel statusTitle = UIStyle.createHeading("System Status", 3);
        statusText.add(statusTitle, "wrap");

        JLabel statusDesc = UIStyle.createBodyLabel("System is operational. All services are running normally.");
        statusText.add(statusDesc);

        statusCard.add(statusText, "growx");

        // Status Badge
        maintenanceStatusLabel = new JLabel(" ONLINE ");
        maintenanceStatusLabel.setFont(UIStyle.FONT_BODY_BOLD);
        maintenanceStatusLabel.setForeground(Color.WHITE);
        maintenanceStatusLabel.setOpaque(true);
        maintenanceStatusLabel.setBackground(UIStyle.SUCCESS_GREEN);
        maintenanceStatusLabel.setBorder(new EmptyBorder(8, 16, 8, 16));

        statusCard.add(maintenanceStatusLabel);

        contentPanel.add(statusCard, "growx, wrap");

        // Quick actions card
        JPanel actionsCard = UIStyle.createCard();
        actionsCard.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]16[]12[]12[]12[]"));

        JLabel actionsTitle = UIStyle.createHeading("Quick Actions", 3);
        actionsCard.add(actionsTitle, "growx, wrap");

        JLabel actionsDesc = UIStyle
                .createBodyLabel("Navigate to specific management sections using the sidebar menu:");
        actionsCard.add(actionsDesc, "growx, wrap");

        addActionItem(actionsCard, "👥 User Management", "Create and manage student and instructor accounts", () -> {
        });
        addActionItem(actionsCard, "📚 Course Management", "Create and manage course offerings", () -> {
        });
        addActionItem(actionsCard, "📖 Section Management", "Create sections and assign instructors", () -> {
        });
        addActionItem(actionsCard, "⚙️ Maintenance", "Control system maintenance mode", () -> {
        });
        addActionItem(actionsCard, "🔒 Change Password", "Update your account password",
                this::showChangePasswordDialog);

        contentPanel.add(actionsCard, "growx");
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
        if (title.equals("Total Users")) {
            totalUsersLabel = valueLabel;
        } else if (title.equals("Students")) {
            totalStudentsLabel = valueLabel;
        } else if (title.equals("Instructors")) {
            totalInstructorsLabel = valueLabel;
        } else if (title.equals("Courses")) {
            totalCoursesLabel = valueLabel;
        } else if (title.equals("Sections")) {
            totalSectionsLabel = valueLabel;
        } else if (title.equals("Active Enrollments")) {
            activeEnrollmentsLabel = valueLabel;
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
        // Admin username is usually 'admin' or passed in.
        // Since we don't have it in constructor, we'll assume 'admin' for now or get it
        // from a session context if available.
        // Given the simplicity, let's assume 'admin' for the default admin dashboard.
        // Ideally MainFrame should pass the username.
        edu.univ.erp.ui.common.ChangePasswordDialog dialog = new edu.univ.erp.ui.common.ChangePasswordDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                new edu.univ.erp.service.AuthService(),
                "admin");
        dialog.setVisible(true);
    }

    public void refreshStats() {
        // Get counts
        var studentsRes = adminAPI.getAllStudents();
        var instructorsRes = adminAPI.getAllInstructors();
        var coursesRes = adminAPI.getAllCourses();
        var sectionsRes = adminAPI.getAllSections();
        var enrollmentsRes = adminAPI.countActiveEnrollments();
        var maintenanceRes = maintenanceAPI.isMaintenanceMode();

        if (studentsRes.isSuccess() && instructorsRes.isSuccess()) {
            int totalStudents = studentsRes.getData().size();
            int totalInstructors = instructorsRes.getData().size();
            int totalUsers = totalStudents + totalInstructors + 1; // +1 for admin

            if (totalUsersLabel != null)
                totalUsersLabel.setText(String.valueOf(totalUsers));
            if (totalStudentsLabel != null)
                totalStudentsLabel.setText(String.valueOf(totalStudents));
            if (totalInstructorsLabel != null)
                totalInstructorsLabel.setText(String.valueOf(totalInstructors));
        }

        if (coursesRes.isSuccess() && totalCoursesLabel != null) {
            totalCoursesLabel.setText(String.valueOf(coursesRes.getData().size()));
        }

        if (sectionsRes.isSuccess() && totalSectionsLabel != null) {
            totalSectionsLabel.setText(String.valueOf(sectionsRes.getData().size()));
        }

        if (enrollmentsRes.isSuccess() && activeEnrollmentsLabel != null) {
            activeEnrollmentsLabel.setText(String.valueOf(enrollmentsRes.getData()));
        }

        // Update maintenance status
        if (maintenanceRes.isSuccess() && maintenanceStatusLabel != null) {
            boolean maintenance = maintenanceRes.getData();
            maintenanceStatusLabel.setText(maintenance ? " MAINTENANCE " : " ONLINE ");
            maintenanceStatusLabel.setBackground(maintenance ? UIStyle.WARNING_ORANGE : UIStyle.SUCCESS_GREEN);
        }
    }
}
