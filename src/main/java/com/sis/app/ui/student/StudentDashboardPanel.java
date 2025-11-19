package com.sis.app.ui.student;

import com.sis.app.service.StudentService;
import com.sis.app.ui.components.DashboardPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern student dashboard with card-based layout
 */
public class StudentDashboardPanel extends DashboardPanel {
    private final StudentService studentService;
    private final String studentId;
    private final List<AbstractStudentPanel> panels = new ArrayList<>();
    
    private final CardLayout cardLayout;
    private final JPanel tabPanel;

    public StudentDashboardPanel(StudentService studentService, String studentId) {
        this.studentService = studentService;
        this.studentId = studentId;
        
        // Create tab navigation
        cardLayout = new CardLayout();
        tabPanel = new JPanel(cardLayout);
        tabPanel.setOpaque(false);
        
        // Create panels
        CourseCatalogPanel courseCatalog = new CourseCatalogPanel(studentService, studentId);
        RegistrationsPanel registrations = new RegistrationsPanel(studentService, studentId);
        TimetablePanel timetable = new TimetablePanel(studentService, studentId);
        GradesPanel grades = new GradesPanel(studentService, studentId);

        panels.add(courseCatalog);
        panels.add(registrations);
        panels.add(timetable);
        panels.add(grades);

        tabPanel.add(courseCatalog, "catalog");
        tabPanel.add(registrations, "registrations");
        tabPanel.add(timetable, "timetable");
        tabPanel.add(grades, "grades");

        add(tabPanel, BorderLayout.CENTER);
        
        // Load initial data
        showTab("catalog");
    }
    
    public void showTab(String tabName) {
        cardLayout.show(tabPanel, tabName);
        // Refresh the active panel
        AbstractStudentPanel activePanel = getActivePanel(tabName);
        if (activePanel != null) {
            activePanel.refreshData();
        }
    }
    
    private AbstractStudentPanel getActivePanel(String tabName) {
        return switch (tabName) {
            case "catalog" -> panels.get(0);
            case "registrations" -> panels.get(1);
            case "timetable" -> panels.get(2);
            case "grades" -> panels.get(3);
            default -> null;
        };
    }

    public void refreshAllTabs() {
        panels.forEach(AbstractStudentPanel::refreshData);
    }
    
    public String getStudentId() {
        return studentId;
    }
}

