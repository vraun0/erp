package edu.univ.erp.ui.instructor;

import edu.univ.erp.service.InstructorService;
import edu.univ.erp.ui.components.DashboardPanel;
import edu.univ.erp.ui.util.UIStyle;

import javax.swing.*;
import java.awt.*;

/**
 * Modern instructor dashboard with card-based layout
 */
public class InstructorDashboardPanel extends DashboardPanel {
    private final edu.univ.erp.api.instructor.InstructorAPI instructorAPI;
    private final String instructorId;

    private final InstructorDashboardView dashboardView;

    public InstructorDashboardPanel(edu.univ.erp.api.instructor.InstructorAPI instructorAPI, String instructorId) {
        this.instructorAPI = instructorAPI;
        this.instructorId = instructorId;

        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_DARK);

        // Initialize sub-panels
        dashboardView = new InstructorDashboardView(instructorAPI, instructorId);

        add(dashboardView, BorderLayout.CENTER);
    }

    public void refreshStats() {
        if (dashboardView != null)
            dashboardView.refreshStats();
    }

    // No longer needed as tabs are handled by MainFrame
    public void showTab(String tabName) {
        // No-op
    }

    public void refreshAllTabs() {
        refreshStats();
    }

    public String getInstructorId() {
        return instructorId;
    }
}
