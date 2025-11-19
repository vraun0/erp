package com.sis.app.ui;

import com.sis.app.model.User;
import com.sis.app.service.AdminService;
import com.sis.app.service.InstructorService;
import com.sis.app.service.StudentService;
import com.sis.app.ui.admin.AdminDashboardPanel;
import com.sis.app.ui.components.SidebarPanel;
import com.sis.app.ui.components.TopBarPanel;
import com.sis.app.ui.instructor.InstructorDashboardPanel;
import com.sis.app.ui.student.StudentDashboardPanel;
import com.sis.app.ui.util.UIStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Modern ERP main window with sidebar navigation and top bar
 */
public class MainFrame extends JFrame {
    private static final String ADMIN_PANEL = "ADMIN";
    private static final String INSTRUCTOR_PANEL = "INSTRUCTOR";
    private static final String STUDENT_PANEL = "STUDENT";

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    
    private final SidebarPanel sidebarPanel;
    private final TopBarPanel topBarPanel;
    
    private final StudentService studentService = new StudentService();
    private final InstructorService instructorService = new InstructorService();
    private final AdminService adminService = new AdminService();

    private StudentDashboardPanel studentDashboardPanel;
    private InstructorDashboardPanel instructorDashboardPanel;
    private AdminDashboardPanel adminDashboardPanel;
    
    private User currentUser;

    public MainFrame() {
        super("Enterprise ERP System");
        
        // Set up modern dark theme
        setBackground(UIStyle.BACKGROUND_DARK);
        getContentPane().setBackground(UIStyle.BACKGROUND_DARK);
        
        // Create main layout: sidebar + topbar + content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIStyle.BACKGROUND_DARK);
        
        // Top bar
        topBarPanel = new TopBarPanel();
        mainPanel.add(topBarPanel, BorderLayout.NORTH);
        
        // Sidebar + Content panel
        JPanel sidebarContentPanel = new JPanel(new BorderLayout());
        sidebarContentPanel.setBackground(UIStyle.BACKGROUND_DARK);
        
        // Sidebar
        sidebarPanel = new SidebarPanel();
        sidebarContentPanel.add(sidebarPanel, BorderLayout.WEST);
        
        // Content area with card layout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIStyle.BACKGROUND_DARK);
        contentPanel.setOpaque(true);
        
        sidebarContentPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(sidebarContentPanel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 1000);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1200, 800));
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                revalidate();
                repaint();
            }
        });
    }

    public void showForUser(User user) {
        this.currentUser = user;
        topBarPanel.setUser(user);
        
        if (user.isAdmin()) {
            setupAdminInterface(user);
        } else if (user.isInstructor()) {
            setupInstructorInterface(user);
        } else {
            setupStudentInterface(user);
        }
    }
    
    private void setupAdminInterface(User user) {
        topBarPanel.setTitle("Admin Dashboard");
        
        // Setup sidebar menu
        sidebarPanel.addMenuItem("dashboard", "📊", "Dashboard", e -> showAdminDashboard());
        sidebarPanel.addMenuItem("users", "👥", "User Management", e -> showUserManagement());
        sidebarPanel.addMenuItem("courses", "📚", "Courses", e -> showCourseManagement());
        sidebarPanel.addMenuItem("sections", "📖", "Sections", e -> showSectionManagement());
        sidebarPanel.addMenuItem("maintenance", "⚙️", "Maintenance", e -> showMaintenance());
        
        // Initialize panels
        ensureAdminPanel(user);
        
        // Show dashboard by default
        sidebarPanel.setActiveItem("dashboard");
        cardLayout.show(contentPanel, ADMIN_PANEL);
    }
    
    private void setupInstructorInterface(User user) {
        topBarPanel.setTitle("Instructor Portal");
        
        sidebarPanel.addMenuItem("sections", "📖", "My Sections", e -> showInstructorSections());
        sidebarPanel.addMenuItem("gradebook", "📋", "Gradebook", e -> showGradebook());
        
        ensureInstructorPanel(user);
        
        sidebarPanel.setActiveItem("sections");
        cardLayout.show(contentPanel, INSTRUCTOR_PANEL);
    }
    
    private void setupStudentInterface(User user) {
        topBarPanel.setTitle("Student Portal");
        
        sidebarPanel.addMenuItem("dashboard", "📊", "Dashboard", e -> showStudentDashboard());
        sidebarPanel.addMenuItem("catalog", "📚", "Course Catalog", e -> showCourseCatalog());
        sidebarPanel.addMenuItem("registrations", "📝", "My Registrations", e -> showRegistrations());
        sidebarPanel.addMenuItem("timetable", "📅", "Timetable", e -> showTimetable());
        sidebarPanel.addMenuItem("grades", "📊", "Grades", e -> showGrades());
        
        ensureStudentPanel(user);
        
        sidebarPanel.setActiveItem("dashboard");
        cardLayout.show(contentPanel, STUDENT_PANEL);
    }
    
    // Admin navigation methods
    private void showAdminDashboard() {
        sidebarPanel.setActiveItem("dashboard");
        cardLayout.show(contentPanel, ADMIN_PANEL);
        if (adminDashboardPanel != null) {
            adminDashboardPanel.showTab("users"); // Default view
        }
    }
    
    private void showUserManagement() {
        sidebarPanel.setActiveItem("users");
        cardLayout.show(contentPanel, ADMIN_PANEL);
        if (adminDashboardPanel != null) {
            adminDashboardPanel.showTab("users");
        }
    }
    
    private void showCourseManagement() {
        sidebarPanel.setActiveItem("courses");
        cardLayout.show(contentPanel, ADMIN_PANEL);
        if (adminDashboardPanel != null) {
            adminDashboardPanel.showTab("courses");
        }
    }
    
    private void showSectionManagement() {
        sidebarPanel.setActiveItem("sections");
        cardLayout.show(contentPanel, ADMIN_PANEL);
        if (adminDashboardPanel != null) {
            adminDashboardPanel.showTab("sections");
        }
    }
    
    private void showMaintenance() {
        sidebarPanel.setActiveItem("maintenance");
        cardLayout.show(contentPanel, ADMIN_PANEL);
        if (adminDashboardPanel != null) {
            adminDashboardPanel.showTab("maintenance");
        }
    }
    
    // Instructor navigation methods
    private void showInstructorSections() {
        sidebarPanel.setActiveItem("sections");
        cardLayout.show(contentPanel, INSTRUCTOR_PANEL);
        if (instructorDashboardPanel != null) {
            instructorDashboardPanel.showTab("sections");
        }
    }
    
    private void showGradebook() {
        sidebarPanel.setActiveItem("gradebook");
        cardLayout.show(contentPanel, INSTRUCTOR_PANEL);
        if (instructorDashboardPanel != null) {
            instructorDashboardPanel.showTab("gradebook");
        }
    }
    
    // Student navigation methods
    private void showStudentDashboard() {
        sidebarPanel.setActiveItem("dashboard");
        cardLayout.show(contentPanel, STUDENT_PANEL);
        if (studentDashboardPanel != null) {
            studentDashboardPanel.showTab("catalog"); // Default view
        }
    }
    
    private void showCourseCatalog() {
        sidebarPanel.setActiveItem("catalog");
        cardLayout.show(contentPanel, STUDENT_PANEL);
        if (studentDashboardPanel != null) {
            studentDashboardPanel.showTab("catalog");
        }
    }
    
    private void showRegistrations() {
        sidebarPanel.setActiveItem("registrations");
        cardLayout.show(contentPanel, STUDENT_PANEL);
        if (studentDashboardPanel != null) {
            studentDashboardPanel.showTab("registrations");
        }
    }
    
    private void showTimetable() {
        sidebarPanel.setActiveItem("timetable");
        cardLayout.show(contentPanel, STUDENT_PANEL);
        if (studentDashboardPanel != null) {
            studentDashboardPanel.showTab("timetable");
        }
    }
    
    private void showGrades() {
        sidebarPanel.setActiveItem("grades");
        cardLayout.show(contentPanel, STUDENT_PANEL);
        if (studentDashboardPanel != null) {
            studentDashboardPanel.showTab("grades");
        }
    }

    private void ensureStudentPanel(User user) {
        if (studentDashboardPanel == null) {
            studentDashboardPanel = new StudentDashboardPanel(studentService, user.getUsername());
            contentPanel.add(studentDashboardPanel, STUDENT_PANEL);
        }
        studentDashboardPanel.refreshAllTabs();
    }

    private void ensureInstructorPanel(User user) {
        if (instructorDashboardPanel == null) {
            instructorDashboardPanel = new InstructorDashboardPanel(instructorService, user.getUsername());
            contentPanel.add(instructorDashboardPanel, INSTRUCTOR_PANEL);
        }
        instructorDashboardPanel.refreshAllTabs();
    }

    private void ensureAdminPanel(User user) {
        if (adminDashboardPanel == null) {
            adminDashboardPanel = new AdminDashboardPanel(adminService);
            contentPanel.add(adminDashboardPanel, ADMIN_PANEL);
        }
        adminDashboardPanel.refreshAll();
    }
}
