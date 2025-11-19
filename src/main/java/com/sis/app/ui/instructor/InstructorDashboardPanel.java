package com.sis.app.ui.instructor;

import com.sis.app.service.InstructorService;
import com.sis.app.ui.components.DashboardPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Modern instructor dashboard with card-based layout
 */
public class InstructorDashboardPanel extends DashboardPanel {
    private final InstructorService instructorService;
    private final String instructorId;
    
    private final SectionsPanel sectionsPanel;
    private final GradebookPanel gradebookPanel;
    
    private final CardLayout cardLayout;
    private final JPanel tabPanel;

    public InstructorDashboardPanel(InstructorService instructorService, String instructorId) {
        this.instructorService = instructorService;
        this.instructorId = instructorId;
        
        cardLayout = new CardLayout();
        tabPanel = new JPanel(cardLayout);
        tabPanel.setOpaque(false);

        sectionsPanel = new SectionsPanel(instructorService, instructorId);
        gradebookPanel = new GradebookPanel(instructorService, instructorId);

        tabPanel.add(sectionsPanel, "sections");
        tabPanel.add(gradebookPanel, "gradebook");

        add(tabPanel, BorderLayout.CENTER);
        
        // Load initial data
        showTab("sections");
    }
    
    public void showTab(String tabName) {
        cardLayout.show(tabPanel, tabName);
        switch (tabName) {
            case "sections" -> sectionsPanel.refreshData();
            case "gradebook" -> gradebookPanel.refreshSections();
        }
    }

    public void refreshAllTabs() {
        sectionsPanel.refreshData();
        gradebookPanel.refreshSections();
    }
    
    public String getInstructorId() {
        return instructorId;
    }
}

