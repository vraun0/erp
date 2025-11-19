package com.sis.app.ui.admin;

import com.sis.app.service.AdminService;
import com.sis.app.ui.components.DashboardPanel;
import com.sis.app.ui.util.UIStyle;

import javax.swing.*;
import java.awt.*;

/**
 * Modern admin dashboard with card-based layout
 */
public class AdminDashboardPanel extends DashboardPanel {
    private final AdminService adminService;
    
    private final UserManagementPanel userPanel;
    private final CourseManagementPanel coursePanel;
    private final SectionManagementPanel sectionPanel;
    private final MaintenancePanel maintenancePanel;
    
    private final CardLayout cardLayout;
    private final JPanel tabPanel;

    public AdminDashboardPanel(AdminService adminService) {
        this.adminService = adminService;
        
        cardLayout = new CardLayout();
        tabPanel = new JPanel(cardLayout);
        tabPanel.setOpaque(false);

        userPanel = new UserManagementPanel(adminService);
        coursePanel = new CourseManagementPanel(adminService);
        sectionPanel = new SectionManagementPanel(adminService);
        maintenancePanel = new MaintenancePanel(adminService);

        tabPanel.add(userPanel, "users");
        tabPanel.add(coursePanel, "courses");
        tabPanel.add(sectionPanel, "sections");
        tabPanel.add(maintenancePanel, "maintenance");

        add(tabPanel, BorderLayout.CENTER);
        
        // Load initial data
        showTab("sections");
    }
    
    public void showTab(String tabName) {
        cardLayout.show(tabPanel, tabName);
        switch (tabName) {
            case "sections" -> sectionPanel.refreshData();
            case "maintenance" -> maintenancePanel.loadState();
        }
    }

    public void refreshAll() {
        sectionPanel.refreshData();
        maintenancePanel.loadState();
    }
}

