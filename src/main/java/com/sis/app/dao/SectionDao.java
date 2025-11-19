package com.sis.app.dao;

import com.sis.app.config.DatabaseManager;
import com.sis.app.model.Section;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SectionDao {
    private static final String SELECT_BY_ID = "SELECT section_id, course_id, instructor_id, day_time, room, capacity, semester, year FROM sections WHERE section_id = ?";
    private static final String SELECT_ALL = "SELECT section_id, course_id, instructor_id, day_time, room, capacity, semester, year FROM sections";
    private static final String INSERT_SECTION = "INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_INSTRUCTOR = "UPDATE sections SET instructor_id = ? WHERE section_id = ?";

    private final DatabaseManager databaseManager;

    public SectionDao() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public Optional<Section> findById(int sectionId) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSection(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Section> findAll() throws SQLException {
        List<Section> sections = new ArrayList<>();
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sections.add(mapSection(rs));
            }
        }
        return sections;
    }

    public int insertSection(Section section) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SECTION, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, section.getCourseId());
            stmt.setString(2, section.getInstructorId());
            stmt.setString(3, section.getDayTime());
            stmt.setString(4, section.getRoom());
            stmt.setInt(5, section.getCapacity());
            stmt.setInt(6, section.getSemester());
            stmt.setInt(7, section.getYear());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create section");
    }

    public void updateInstructor(int sectionId, String instructorId) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_INSTRUCTOR)) {
            stmt.setString(1, instructorId);
            stmt.setInt(2, sectionId);
            stmt.executeUpdate();
        }
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
