package com.sis.app.dao;

import com.sis.app.config.DatabaseManager;
import com.sis.app.model.Course;
import com.sis.app.model.Section;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourseDao {
    private static final String SELECT_ALL_COURSES = "SELECT code, title, credits FROM courses ORDER BY code";
    private static final String SELECT_COURSE_BY_CODE = "SELECT code, title, credits FROM courses WHERE code = ?";
    private static final String SELECT_SECTIONS_FOR_COURSE = "SELECT section_id, course_id, instructor_id, day_time, room, capacity, semester, year FROM sections WHERE course_id = ? ORDER BY year DESC, semester DESC, section_id";
    private static final String INSERT_COURSE = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";

    private final DatabaseManager databaseManager;

    public CourseDao() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public List<Course> findAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_COURSES);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Course course = new Course();
                course.setCode(rs.getString("code"));
                course.setTitle(rs.getString("title"));
                course.setCredits(rs.getInt("credits"));
                courses.add(course);
            }
        }
        return courses;
    }

    public Optional<Course> findByCode(String courseCode) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_COURSE_BY_CODE)) {

            stmt.setString(1, courseCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Course course = new Course();
                    course.setCode(rs.getString("code"));
                    course.setTitle(rs.getString("title"));
                    course.setCredits(rs.getInt("credits"));
                    return Optional.of(course);
                }
            }
        }
        return Optional.empty();
    }

    public void insertCourse(Course course) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_COURSE)) {
            stmt.setString(1, course.getCode());
            stmt.setString(2, course.getTitle());
            stmt.setInt(3, course.getCredits());
            stmt.executeUpdate();
        }
    }

    public List<Section> findSectionsForCourse(String courseCode) throws SQLException {
        List<Section> sections = new ArrayList<>();
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_SECTIONS_FOR_COURSE)) {

            stmt.setString(1, courseCode);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sections.add(mapSection(rs));
                }
            }
        }
        return sections;
    }

    private Section mapSection(ResultSet rs) throws SQLException {
        Section section = new Section();
        section.setSectionId(rs.getInt("section_id"));
        section.setCourseId(rs.getString("course_id"));
        section.setInstructorId(rs.getString("instructor_id"));
        section.setDayTime(rs.getString("day_time"));
        section.setRoom(rs.getString("room"));
        section.setCapacity(rs.getInt("capacity"));
        section.setSemester(rs.getInt("semester"));
        section.setYear(rs.getInt("year"));
        return section;
    }
}
