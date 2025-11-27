package edu.univ.erp.ui.admin;

import edu.univ.erp.api.admin.AdminAPI;
import edu.univ.erp.api.maintenance.MaintenanceAPI;
import edu.univ.erp.ui.components.DashboardPanel;
import edu.univ.erp.ui.util.UIStyle;

import javax.swing.*;
import java.awt.*;

/**
 * Modern admin dashboard with card-based layout
 */
public class AdminDashboardPanel extends DashboardPanel {
    private final AdminAPI adminService;

    private final AdminDashboardView dashboardView;
    private final UserManagementPanel userPanel;
    private final CourseManagementPanel coursePanel;
    private final SectionManagementPanel sectionPanel;
    private final MaintenancePanel maintenancePanel;

    private final CardLayout cardLayout;
    private final JPanel tabPanel;

    public AdminDashboardPanel(AdminAPI adminAPI, MaintenanceAPI maintenanceAPI) {
        this.adminService = adminAPI;

        cardLayout = new CardLayout();
        tabPanel = new JPanel(cardLayout);
        tabPanel.setOpaque(false);

        dashboardView = new AdminDashboardView(adminAPI, maintenanceAPI);
        userPanel = new UserManagementPanel(adminAPI);
        coursePanel = new CourseManagementPanel(adminAPI);
        sectionPanel = new SectionManagementPanel(adminAPI);
        maintenancePanel = new MaintenancePanel(maintenanceAPI);

        tabPanel.add(dashboardView, "dashboard");
        tabPanel.add(userPanel, "users");
        tabPanel.add(coursePanel, "courses");
        tabPanel.add(sectionPanel, "sections");
        tabPanel.add(maintenancePanel, "maintenance");

        add(tabPanel, BorderLayout.CENTER);

        // Load initial data
        showTab("dashboard");
    }

    public void showTab(String tabName) {
        cardLayout.show(tabPanel, tabName);
        switch (tabName) {
            case "dashboard" -> dashboardView.refreshStats();
            case "sections" -> sectionPanel.refreshData();
            case "maintenance" -> maintenancePanel.loadState();
        }
    }

    public void refreshAll() {
        dashboardView.refreshStats();
        sectionPanel.refreshData();
        maintenancePanel.loadState();
    }
}
