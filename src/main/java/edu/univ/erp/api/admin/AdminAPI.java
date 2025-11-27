package edu.univ.erp.api.admin;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.types.CourseRow;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.api.types.StudentRow;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.model.Course;
import edu.univ.erp.model.Section;
import edu.univ.erp.model.Student;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AdminAPI {
    private final AdminService adminService;

    public AdminAPI() {
        this.adminService = new AdminService();
    }

    public ApiResponse<Void> createStudent(String username, String password, int rollNumber, String program, int year) {
        try {
            adminService.createStudentAccount(username, password, rollNumber, program, year);
            return ApiResponse.success(null);
        } catch (SQLException | ServiceException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<Void> createInstructor(String username, String password, String department) {
        try {
            adminService.createInstructorAccount(username, password, department);
            return ApiResponse.success(null);
        } catch (SQLException | ServiceException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<Void> createCourse(String code, String title, int credits) {
        try {
            adminService.createCourse(code, title, credits);
            return ApiResponse.success(null);
        } catch (SQLException | ServiceException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<Void> createSection(String courseCode, Integer instructorId, String dayTime, String room,
            int capacity, int semester, int year, java.time.LocalDate dropDeadline) {
        try {
            adminService.createSection(courseCode, instructorId, dayTime, room, capacity, semester, year, dropDeadline);
            return ApiResponse.success(null);
        } catch (SQLException | ServiceException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<CourseRow>> getAllCourses() {
        try {
            List<Course> courses = adminService.getAllCourses();
            List<CourseRow> rows = courses.stream()
                    .map(c -> new CourseRow(c.getCode(), c.getTitle(), c.getCredits()))
                    .collect(Collectors.toList());
            return ApiResponse.success(rows);
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        }
    }

    public ApiResponse<List<edu.univ.erp.api.types.InstructorRow>> getAllInstructors() {
        try {
            List<edu.univ.erp.model.Instructor> instructors = adminService.getAllInstructors();
            List<edu.univ.erp.api.types.InstructorRow> rows = new java.util.ArrayList<>();
            for (edu.univ.erp.model.Instructor i : instructors) {
                edu.univ.erp.model.User user = adminService.getUser(i.getUserId());
                String name = user != null ? user.getUsername() : "Unknown";
                rows.add(new edu.univ.erp.api.types.InstructorRow(i.getUserId(), name, i.getDepartment()));
            }
            return ApiResponse.success(rows);
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        }
    }

    public ApiResponse<List<SectionRow>> getAllSections() {
        try {
            List<edu.univ.erp.model.Section> sections = adminService.getAllSections();
            List<SectionRow> rows = new java.util.ArrayList<>();
            for (edu.univ.erp.model.Section s : sections) {
                int enrolled = adminService.getEnrollmentCount(s.getSectionId());
                String instructorName = "TBD";
                if (s.getInstructorId() != null) {
                    edu.univ.erp.model.User u = adminService.getUser(s.getInstructorId());
                    if (u != null) {
                        instructorName = u.getUsername();
                    }
                }
                rows.add(new SectionRow(s.getSectionId(), s.getCourseId(), s.getDayTime(), s.getRoom(), s.getCapacity(),
                        enrolled, instructorName));
            }
            return ApiResponse.success(rows);
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        }
    }

    public ApiResponse<List<StudentRow>> getAllStudents() {
        try {
            List<edu.univ.erp.model.Student> students = adminService.getAllStudents();
            List<StudentRow> rows = new java.util.ArrayList<>();
            for (edu.univ.erp.model.Student s : students) {
                edu.univ.erp.model.User u = adminService.getUser(s.getUserId());
                String name = u != null ? u.getUsername() : "Unknown";
                rows.add(new StudentRow(s.getUserId(), name, s.getRollNumber(), s.getProgram()));
            }
            return ApiResponse.success(rows);
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        }
    }

    public ApiResponse<Void> assignInstructor(int sectionId, int instructorId) {
        try {
            adminService.assignInstructor(sectionId, instructorId);
            return ApiResponse.success(null);
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        }
    }

    public ApiResponse<Void> deleteSection(int sectionId) {
        try {
            adminService.deleteSection(sectionId);
            return ApiResponse.success(null);
        } catch (SQLException | ServiceException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<Integer> countActiveEnrollments() {
        try {
            return ApiResponse.success(adminService.countActiveEnrollments());
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        }
    }

    // Additional methods can be added as needed for full coverage
}
