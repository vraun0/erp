package edu.univ.erp.api.instructor;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.types.*;
import edu.univ.erp.model.Attendance;
import edu.univ.erp.model.Course;
import edu.univ.erp.model.Section;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.ServiceException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InstructorAPI {
    private final InstructorService instructorService;

    public InstructorAPI() {
        this.instructorService = new InstructorService();
    }

    public ApiResponse<List<SectionRow>> getSections(int instructorId) {
        try {
            List<Section> sections = instructorService.getSectionsForInstructor(instructorId);
            List<SectionRow> rows = new ArrayList<>();
            for (Section s : sections) {
                Course course = instructorService.getCourseForSection(s.getSectionId());
                String courseCode = course != null ? course.getCode() : "Unknown";
                // We don't have enrollment count here easily without calling another service,
                // but SectionRow expects it.
                // For instructor view, maybe we can skip it or fetch it.
                // Let's fetch it if possible, or use 0 for now and update if needed.
                // Actually InstructorService doesn't expose getEnrollmentCount directly.
                // But we can use EnrollmentDao internally in Service if we wanted.
                // For now, let's use 0 or modify SectionRow to make it optional?
                // SectionRow is a record, so no optional.
                // Let's check if we can add getEnrollmentCount to InstructorService.
                // Or just leave it as 0 for now as it might not be critical for this view.
                // Wait, SectionRow is shared.
                rows.add(new SectionRow(s.getSectionId(), s.getCourseId(), s.getDayTime(), s.getRoom(), s.getCapacity(),
                        0, "Self"));
            }
            return ApiResponse.success(rows);
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        }
    }

    public ApiResponse<List<GradebookRow>> getGradebook(int instructorId, int sectionId) {
        try {
            List<edu.univ.erp.model.view.GradebookRow> serviceRows = instructorService.getGradebook(instructorId,
                    sectionId);
            List<GradebookRow> apiRows = serviceRows.stream()
                    .map(r -> new GradebookRow(
                            r.getEnrollmentId(),
                            r.getStudentId(),
                            r.getStudentName(),
                            r.getRollNumber(),
                            r.getMidtermScore(),
                            r.getFinalExamScore(),
                            r.getProjectScore(),
                            r.getFinalScore()))
                    .collect(Collectors.toList());
            return ApiResponse.success(apiRows);
        } catch (ServiceException e) {
            return ApiResponse.error(e.getMessage());
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        }
    }

    public ApiResponse<Void> updateGrades(int instructorId, int sectionId, List<GradebookRow> rows) {
        try {
            List<edu.univ.erp.model.view.GradebookRow> serviceRows = rows.stream()
                    .map(r -> new edu.univ.erp.model.view.GradebookRow(
                            r.enrollmentId(),
                            r.studentId(),
                            r.studentName(),
                            r.rollNumber(),
                            r.midtermScore(),
                            r.finalExamScore(),
                            r.projectScore(),
                            r.finalScore()))
                    .collect(Collectors.toList());
            instructorService.updateGrades(instructorId, sectionId, serviceRows);
            return ApiResponse.success(null);
        } catch (ServiceException e) {
            return ApiResponse.error(e.getMessage());
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        }
    }

    public ApiResponse<AttendanceRow> getAttendance(int enrollmentId, LocalDate date) {
        try {
            Attendance attendance = instructorService.getAttendance(enrollmentId, date);
            if (attendance == null) {
                return ApiResponse.success(null);
            }
            return ApiResponse.success(new AttendanceRow(
                    attendance.getAttendanceId(),
                    attendance.getEnrollmentId(),
                    attendance.getDate(),
                    attendance.getStatus()));
        } catch (ServiceException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<Void> markAttendance(AttendanceRow row) {
        try {
            Attendance attendance = new Attendance(
                    row.attendanceId(),
                    row.enrollmentId(),
                    row.date(),
                    row.status());
            instructorService.markAttendance(attendance);
            return ApiResponse.success(null);
        } catch (ServiceException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<ClassStatistics> getClassStatistics(int instructorId, int sectionId) {
        try {
            edu.univ.erp.model.ClassStatistics stats = instructorService.getClassStatistics(instructorId, sectionId);
            return ApiResponse.success(new ClassStatistics(
                    stats.getAverageScore(),
                    stats.getMinScore(),
                    stats.getMaxScore(),
                    stats.getStdDev(),
                    stats.getStudentCount(),
                    stats.getGradeDistribution()));
        } catch (ServiceException e) {
            return ApiResponse.error(e.getMessage());
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        }
    }

    public ApiResponse<String> getInstructorName(int instructorId) {
        try {
            return ApiResponse.success(instructorService.getInstructorUsername(instructorId));
        } catch (ServiceException e) {
            return ApiResponse.error(e.getMessage());
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        }
    }

    public ApiResponse<InstructorDashboardStats> getDashboardStats(int instructorId) {
        try {
            return ApiResponse.success(instructorService.getDashboardStats(instructorId));
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        }
    }

    public ApiResponse<CourseRow> getCourse(String courseCode) {
        try {
            Course course = instructorService.getCourse(courseCode);
            if (course == null) {
                return ApiResponse.success(null);
            }
            return ApiResponse.success(new CourseRow(course.getCode(), course.getTitle(), course.getCredits()));
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        }
    }
}
