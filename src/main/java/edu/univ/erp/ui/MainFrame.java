package edu.univ.erp.ui;

import edu.univ.erp.model.User;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.ui.admin.AdminDashboardPanel;
import edu.univ.erp.ui.components.SidebarPanel;
import edu.univ.erp.ui.components.TopBarPanel;
import edu.univ.erp.ui.instructor.AttendancePanel;
import edu.univ.erp.ui.instructor.GradebookPanel;
import edu.univ.erp.ui.instructor.InstructorDashboardPanel;
import edu.univ.erp.ui.instructor.SectionsPanel;
import edu.univ.erp.ui.student.*;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Modern ERP main window with sidebar navigation and top bar
 */
public class MainFrame extends JFrame {
    // Card Names
    private static final String ADMIN_PANEL = "ADMIN";

    // Instructor Cards
    private static final String INSTRUCTOR_DASHBOARD = "INSTRUCTOR_DASHBOARD";
    private static final String INSTRUCTOR_SECTIONS = "INSTRUCTOR_SECTIONS";
    private static final String INSTRUCTOR_GRADEBOOK = "INSTRUCTOR_GRADEBOOK";
    private static final String INSTRUCTOR_ATTENDANCE = "INSTRUCTOR_ATTENDANCE";

    // Student Cards
    private static final String STUDENT_DASHBOARD = "STUDENT_DASHBOARD";
    private static final String STUDENT_CATALOG = "STUDENT_CATALOG";
    private static final String STUDENT_REGISTRATIONS = "STUDENT_REGISTRATIONS";
    private static final String STUDENT_TIMETABLE = "STUDENT_TIMETABLE";
    private static final String STUDENT_GRADES = "STUDENT_GRADES";
    private static final String STUDENT_FEES = "STUDENT_FEES";

    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    private final SidebarPanel sidebarPanel;
    private final TopBarPanel topBarPanel;

    private final StudentService studentService = new StudentService();
    // InstructorService replaced by InstructorAPI
    private final edu.univ.erp.api.admin.AdminAPI adminAPI = new edu.univ.erp.api.admin.AdminAPI();
    private final edu.univ.erp.api.instructor.InstructorAPI instructorAPI = new edu.univ.erp.api.instructor.InstructorAPI();

    // Panels
    private AdminDashboardPanel adminDashboardPanel;

    private InstructorDashboardPanel instructorDashboardPanel;
    private SectionsPanel instructorSectionsPanel;
    private GradebookPanel instructorGradebookPanel;
    private AttendancePanel instructorAttendancePanel;

    private StudentDashboardPanel studentDashboardPanel;
    private CourseCatalogPanel studentCatalogPanel;
    private RegistrationsPanel studentRegistrationsPanel;
    private TimetablePanel studentTimetablePanel;
    private GradesPanel studentGradesPanel;
    private FeesPanel studentFeesPanel;

    private User currentUser;

    private final edu.univ.erp.api.maintenance.MaintenanceAPI maintenanceAPI = new edu.univ.erp.api.maintenance.MaintenanceAPI();
    private JPanel maintenancePanel;

    public MainFrame() {
        super("Enterprise ERP System");

        // Set up modern dark theme
        setBackground(UIStyle.BACKGROUND_DARK);
        getContentPane().setBackground(UIStyle.BACKGROUND_DARK);

        // Create main layout: sidebar + topbar + content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIStyle.BACKGROUND_DARK);

        // Maintenance Banner (Hidden by default)
        maintenancePanel = new JPanel(new MigLayout("fillx, insets 8 20 8 20", "[center]", "[]"));
        maintenancePanel.setBackground(UIStyle.WARNING_ORANGE);
        JLabel maintenanceLabel = new JLabel("⚠ SYSTEM IS IN MAINTENANCE MODE - LIMITED ACCESS");
        maintenanceLabel.setFont(UIStyle.FONT_BODY_BOLD);
        maintenanceLabel.setForeground(Color.WHITE);
        maintenancePanel.add(maintenanceLabel);
        maintenancePanel.setVisible(false);
        mainPanel.add(maintenancePanel, BorderLayout.NORTH);

        // Top bar container to hold banner and topbar
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(maintenancePanel, BorderLayout.NORTH);

        // Top bar
        topBarPanel = new TopBarPanel();
        topContainer.add(topBarPanel, BorderLayout.CENTER);

        mainPanel.add(topContainer, BorderLayout.NORTH);

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
        try {
            this.currentUser = user;
            topBarPanel.setUser(user);

            // Check maintenance mode
            edu.univ.erp.api.common.ApiResponse<Boolean> maintenance = maintenanceAPI.isMaintenanceMode();
            maintenancePanel.setVisible(maintenance.isSuccess() && maintenance.getData());

            // Clear existing panels to ensure fresh state
            contentPanel.removeAll();

            if (user.isAdmin()) {
                setupAdminInterface(user);
            } else if (user.isInstructor()) {
                setupInstructorInterface(user);
            } else {
                setupStudentInterface(user);
            }

            // Ensure the frame is ready to be displayed
            revalidate();
            repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error initializing dashboard: " + ex.getMessage(),
                    "Initialization Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupAdminInterface(User user) {
        topBarPanel.setTitle("Admin Dashboard");
        topBarPanel.hideSearch();

        sidebarPanel.clearMenu();
        sidebarPanel.addMenuItem("dashboard", "📊", "Dashboard", e -> showAdminDashboard());
        sidebarPanel.addMenuItem("users", "👥", "User Management", e -> showUserManagement());
        sidebarPanel.addMenuItem("courses", "📚", "Courses", e -> showCourseManagement());
        sidebarPanel.addMenuItem("sections", "📖", "Sections", e -> showSectionManagement());
        sidebarPanel.addMenuItem("maintenance", "⚙️", "Maintenance", e -> showMaintenance());
        sidebarPanel.addMenuItem("password", "🔒", "Change Password", e -> showChangePassword());
        sidebarPanel.addMenuItem("logout", "🚪", "Logout", e -> logout());

        // Initialize Admin Panel
        adminDashboardPanel = new AdminDashboardPanel(adminAPI, maintenanceAPI);
        contentPanel.add(adminDashboardPanel, ADMIN_PANEL);

        // Show dashboard by default
        showAdminDashboard();
    }

    private void setupInstructorInterface(User user) {
        topBarPanel.setTitle("Instructor Portal");
        topBarPanel.hideSearch();

        sidebarPanel.clearMenu();
        sidebarPanel.addMenuItem("dashboard", "📊", "Dashboard", e -> showInstructorDashboard());
        sidebarPanel.addMenuItem("sections", "📖", "My Sections", e -> showInstructorSections());
        sidebarPanel.addMenuItem("gradebook", "📋", "Gradebook", e -> showInstructorGradebook());
        sidebarPanel.addMenuItem("attendance", "📅", "Attendance", e -> showInstructorAttendance());
        sidebarPanel.addMenuItem("password", "🔒", "Change Password", e -> showChangePassword());
        sidebarPanel.addMenuItem("logout", "🚪", "Logout", e -> logout());

        // Initialize Instructor Panels
        String instructorId = String.valueOf(user.getUserId());
        instructorDashboardPanel = new InstructorDashboardPanel(instructorAPI, instructorId);
        instructorSectionsPanel = new SectionsPanel(instructorAPI, instructorId);
        instructorGradebookPanel = new GradebookPanel(instructorAPI, instructorId);
        instructorAttendancePanel = new AttendancePanel(instructorAPI, instructorId);

        contentPanel.add(instructorDashboardPanel, INSTRUCTOR_DASHBOARD);
        contentPanel.add(instructorSectionsPanel, INSTRUCTOR_SECTIONS);
        contentPanel.add(instructorGradebookPanel, INSTRUCTOR_GRADEBOOK);
        contentPanel.add(instructorAttendancePanel, INSTRUCTOR_ATTENDANCE);

        showInstructorDashboard();
    }

    private void setupStudentInterface(User user) {
        topBarPanel.setTitle("Student Portal");
        topBarPanel.hideSearch();

        sidebarPanel.clearMenu();
        sidebarPanel.addMenuItem("dashboard", "📊", "Dashboard", e -> showStudentDashboard());
        sidebarPanel.addMenuItem("catalog", "📚", "Course Catalog", e -> showStudentCatalog());
        sidebarPanel.addMenuItem("registrations", "📝", "My Registrations", e -> showStudentRegistrations());
        sidebarPanel.addMenuItem("timetable", "📅", "Timetable", e -> showStudentTimetable());
        sidebarPanel.addMenuItem("grades", "📋", "Grades", e -> showStudentGrades());
        sidebarPanel.addMenuItem("fees", "💰", "Fees", e -> showStudentFees());
        sidebarPanel.addMenuItem("password", "🔒", "Change Password", e -> showChangePassword());
        sidebarPanel.addMenuItem("logout", "🚪", "Logout", e -> logout());

        // Initialize Student Panels
        String studentId = String.valueOf(user.getUserId());
        studentDashboardPanel = new StudentDashboardPanel(studentService, studentId);
        studentCatalogPanel = new CourseCatalogPanel(studentService, studentId);
        studentRegistrationsPanel = new RegistrationsPanel(studentService, studentId);
        studentTimetablePanel = new TimetablePanel(studentService, studentId);
        studentGradesPanel = new GradesPanel(studentService, studentId);
        studentFeesPanel = new FeesPanel(studentService, studentId);

        contentPanel.add(studentDashboardPanel, STUDENT_DASHBOARD);
        contentPanel.add(studentCatalogPanel, STUDENT_CATALOG);
        contentPanel.add(studentRegistrationsPanel, STUDENT_REGISTRATIONS);
        contentPanel.add(studentTimetablePanel, STUDENT_TIMETABLE);
        contentPanel.add(studentGradesPanel, STUDENT_GRADES);
        contentPanel.add(studentFeesPanel, STUDENT_FEES);

        showStudentDashboard();
    }

    // Admin Navigation
    private void showAdminDashboard() {
        sidebarPanel.setActiveItem("dashboard");
        cardLayout.show(contentPanel, ADMIN_PANEL);
        if (adminDashboardPanel != null)
            adminDashboardPanel.showTab("dashboard");
    }

    private void showUserManagement() {
        sidebarPanel.setActiveItem("users");
        cardLayout.show(contentPanel, ADMIN_PANEL);
        if (adminDashboardPanel != null)
            adminDashboardPanel.showTab("users");
    }

    private void showCourseManagement() {
        sidebarPanel.setActiveItem("courses");
        cardLayout.show(contentPanel, ADMIN_PANEL);
        if (adminDashboardPanel != null)
            adminDashboardPanel.showTab("courses");
    }

    private void showSectionManagement() {
        sidebarPanel.setActiveItem("sections");
        cardLayout.show(contentPanel, ADMIN_PANEL);
        if (adminDashboardPanel != null)
            adminDashboardPanel.showTab("sections");
    }

    private void showMaintenance() {
        sidebarPanel.setActiveItem("maintenance");
        cardLayout.show(contentPanel, ADMIN_PANEL);
        if (adminDashboardPanel != null)
            adminDashboardPanel.showTab("maintenance");
    }

    // Instructor Navigation
    private void showInstructorDashboard() {
        sidebarPanel.setActiveItem("dashboard");
        cardLayout.show(contentPanel, INSTRUCTOR_DASHBOARD);
        if (instructorDashboardPanel != null)
            instructorDashboardPanel.refreshStats();
    }

    private void showInstructorSections() {
        sidebarPanel.setActiveItem("sections");
        cardLayout.show(contentPanel, INSTRUCTOR_SECTIONS);
        if (instructorSectionsPanel != null)
            instructorSectionsPanel.refreshData();
    }

    private void showInstructorGradebook() {
        sidebarPanel.setActiveItem("gradebook");
        cardLayout.show(contentPanel, INSTRUCTOR_GRADEBOOK);
        if (instructorGradebookPanel != null)
            instructorGradebookPanel.refreshSections();
    }

    private void showInstructorAttendance() {
        sidebarPanel.setActiveItem("attendance");
        cardLayout.show(contentPanel, INSTRUCTOR_ATTENDANCE);
        // Attendance panel refresh logic if needed
    }

    // Student Navigation
    private void showStudentDashboard() {
        sidebarPanel.setActiveItem("dashboard");
        cardLayout.show(contentPanel, STUDENT_DASHBOARD);
        if (studentDashboardPanel != null)
            studentDashboardPanel.refreshStats();
    }

    private void showStudentCatalog() {
        sidebarPanel.setActiveItem("catalog");
        cardLayout.show(contentPanel, STUDENT_CATALOG);
        if (studentCatalogPanel != null)
            studentCatalogPanel.refreshData();
    }

    private void showStudentRegistrations() {
        sidebarPanel.setActiveItem("registrations");
        cardLayout.show(contentPanel, STUDENT_REGISTRATIONS);
        if (studentRegistrationsPanel != null)
            studentRegistrationsPanel.refreshData();
    }

    private void showStudentTimetable() {
        sidebarPanel.setActiveItem("timetable");
        cardLayout.show(contentPanel, STUDENT_TIMETABLE);
        if (studentTimetablePanel != null)
            studentTimetablePanel.refreshData();
    }

    private void showStudentGrades() {
        sidebarPanel.setActiveItem("grades");
        cardLayout.show(contentPanel, STUDENT_GRADES);
        if (studentGradesPanel != null)
            studentGradesPanel.refreshData();
    }

    private void showStudentFees() {
        sidebarPanel.setActiveItem("fees");
        cardLayout.show(contentPanel, STUDENT_FEES);
        // Fees panel refresh logic if needed
    }

    private void showChangePassword() {
        edu.univ.erp.service.AuthService authService = new edu.univ.erp.service.AuthService();
        edu.univ.erp.ui.common.ChangePasswordDialog dialog = new edu.univ.erp.ui.common.ChangePasswordDialog(this,
                authService, currentUser.getUsername());
        dialog.setVisible(true);
    }

    private void logout() {
        edu.univ.erp.service.AuthService authService = new edu.univ.erp.service.AuthService();
        LoginFrame loginFrame = new LoginFrame(authService, new MainFrame());
        loginFrame.setVisible(true);
        dispose();
    }
}
